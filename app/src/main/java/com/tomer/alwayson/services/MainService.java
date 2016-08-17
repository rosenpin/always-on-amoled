package com.tomer.alwayson.services;

import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomer.alwayson.activities.DummyActivity;
import com.tomer.alwayson.Constants;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.CurrentAppResolver;
import com.tomer.alwayson.helpers.DisplaySize;
import com.tomer.alwayson.helpers.DozeManager;
import com.tomer.alwayson.helpers.Flashlight;
import com.tomer.alwayson.helpers.GreenifyStarter;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.SamsungHelper;
import com.tomer.alwayson.helpers.TTS;
import com.tomer.alwayson.helpers.UncoughtExcepction;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.helpers.ViewUtils;
import com.tomer.alwayson.R;
import com.tomer.alwayson.receivers.ScreenReceiver;
import com.tomer.alwayson.receivers.UnlockReceiver;
import com.tomer.alwayson.views.BatteryView;
import com.tomer.alwayson.views.Clock;
import com.tomer.alwayson.views.DateView;
import com.tomer.alwayson.views.FontAdapter;
import com.tomer.alwayson.views.IconsWrapper;
import com.tomer.alwayson.views.MessageBox;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service implements SensorEventListener, ContextConstatns {
    private boolean demo;
    private boolean refreshing;
    private int originalBrightness = 100;
    private int originalAutoBrightnessStatus;
    private SamsungHelper samsungHelper;
    private DozeManager dozeManager;
    private MessageBox notificationsMessageBox;
    private TTS tts;
    private DateView dateView;
    private BatteryView batteryView;
    private Clock clock;
    private Prefs prefs;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private LinearLayout mainView;
    private WindowManager.LayoutParams windowParams;
    private PowerManager.WakeLock stayAwakeWakeLock;
    private UnlockReceiver unlockReceiver;
    private IconsWrapper iconsWrapper;
    private PowerManager.WakeLock proximityToTurnOff;
    private SensorManager sensorManager;
    private CurrentAppResolver currentAppResolver;
    public static boolean stoppedByShortcut;

    @Override
    public int onStartCommand(Intent origIntent, int flags, int startId) {
        if (windowParams == null) {
            windowParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 65794, -2);
            if (origIntent != null) {
                demo = origIntent.getBooleanExtra("demo", false);
                windowParams.type = origIntent.getBooleanExtra("demo", false) ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            } else
                windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            if (prefs.orientation.equals("horizontal"))
                //Setting screen orientation if horizontal
                windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return super.onStartCommand(origIntent, flags, startId);
                }

            windowManager.addView(frameLayout, windowParams);
        }
        return super.onStartCommand(origIntent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new UncoughtExcepction(this));

        Globals.isServiceRunning = true;
        Log.d(MAIN_SERVICE_LOG_TAG, "Main service has started");
        prefs = new Prefs(getApplicationContext());
        prefs.apply();
        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        originalAutoBrightnessStatus = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 100);

        //Initialize stopped by shortcut
        stoppedByShortcut = false;

        // Setup UI
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        setTheme(R.style.AppTheme);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        frameLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    return gestureAction(prefs.volumeButtonsAction) || prefs.disableVolumeKeys;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    gestureAction(prefs.backButtonAction);
                }
                return super.dispatchKeyEvent(event);
            }
        };
        if (prefs.doubleTapAction != ACTION_OFF_GESTURE || prefs.swipeAction != ACTION_OFF_GESTURE)
            frameLayout.setOnTouchListener(new OnDismissListener(this));
        frameLayout.setBackgroundColor(Color.BLACK);
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mainView = (LinearLayout) (layoutInflater.inflate(R.layout.clock_widget, frameLayout).findViewById(R.id.watchface_wrapper));
        iconsWrapper = (IconsWrapper) mainView.findViewById(R.id.icons_wrapper);
        notificationsMessageBox = (MessageBox) mainView.findViewById(R.id.notifications_box);

        setUpElements();

        FrameLayout.LayoutParams mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mainLayoutParams.gravity = Gravity.CENTER;

        mainView.setLayoutParams(mainLayoutParams);

        unlockReceiver = new UnlockReceiver();
        IntentFilter intentFilter = new IntentFilter();

        notificationsMessageBox.init(prefs.orientation.equals(HORIZONTAL));

        //Adding the intent from the pre-defined array filters
        for (String filter : Constants.unlockFilters) {
            intentFilter.addAction(filter);
        }
        unregisterUnlockReceiver();
        registerReceiver(unlockReceiver, intentFilter);

        // Sensor handling
        if (prefs.proximityToLock || prefs.autoNightMode) //If any sensor is required
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //If proximity option is on, set it up
        if (prefs.proximityToLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Shell.SU.available()) {
                proximityToTurnOff = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, getPackageName() + " wakelock_holder");
                proximityToTurnOff.acquire();
            } else {
                Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (proximitySensor != null) {
                    Log.d(MAIN_SERVICE_LOG_TAG, "STARTING PROXIMITY SENSOR");
                    sensorManager.registerListener(this, proximitySensor, (int) TimeUnit.MILLISECONDS.toMicros(900), 100000);
                }
            }
        }
        //If auto night mode option is on, set it up
        if (prefs.autoNightMode) {
            Sensor lightSensor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT, false);
            } else {
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            }
            if (lightSensor != null) {
                Log.d(MAIN_SERVICE_LOG_TAG, "STARTING LIGHT SENSOR");
                sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15), 500000);
            }
        }

        //Delay to stop
        if (prefs.stopDelay > DISABLED) {
            final int delayInMilliseconds = prefs.stopDelay * 1000 * 60;
            Log.d(MAIN_SERVICE_LOG_TAG, "Setting delay to stop in minutes " + prefs.stopDelay);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stoppedByShortcut = true;
                    stopSelf();
                    stayAwakeWakeLock.release();
                    Globals.killedByDelay = true;
                    Log.d(MAIN_SERVICE_LOG_TAG, "Stopping service after delay");
                }
            }, delayInMilliseconds);
        }

        // UI refreshing
        Globals.notificationChanged = true; //Show notifications at first launch
        if (prefs.notificationsAlerts)
            startService(new Intent(getApplicationContext(), NotificationListener.class)); //Starting notification listener service
        refresh();
        refreshLong(true);

        //Turn lights on
        setLights(ON, false, true);

        //Notification setup
        Globals.onNotificationAction = new Runnable() {
            @Override
            public void run() {
                Log.d("notificaiotns", "New notification!!!");
                iconsWrapper.update(getApplicationContext(), prefs.notificationsAlerts, prefs.textColor, new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                });
                if (Globals.newNotification != null && prefs.notificationPreview) {
                    notificationsMessageBox.showNotification(Globals.newNotification);
                    notificationsMessageBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            stoppedByShortcut = true;
                            if (notificationsMessageBox.getCurrentNotification().getIntent() != null) {
                                try {
                                    startActivity(new Intent(getApplicationContext(), DummyActivity.class));
                                    notificationsMessageBox.getCurrentNotification().getIntent().send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                                stoppedByShortcut = true;
                                stopSelf();
                            }
                        }
                    });
                }
            }
        };
        Globals.onNotificationAction.run();

        //Turn screen on
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        //Greenify integration
                        new GreenifyStarter(getApplicationContext()).start(prefs.greenifyEnabled && !demo);
                        //Turn on the display
                        if (!stayAwakeWakeLock.isHeld()) stayAwakeWakeLock.acquire();
                    }
                },
                500);

        //Initializing Doze
        if (prefs.dozeMode) {
            dozeManager = new DozeManager(this);
            dozeManager.enterDoze();
        }

        //Start the current app resolver and stop the service accordingly
        currentAppResolver = new CurrentAppResolver(this, new int[]{prefs.stopOnCamera ? CurrentAppResolver.CAMERA : 0, prefs.stopOnGoogleNow ? CurrentAppResolver.GOOGLE_NOW : 0});
        currentAppResolver.executeForCurrentApp(new Runnable() {
            @Override
            public void run() {
                Globals.waitingForApp = true;
                stopSelf();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Globals.waitingForApp = false;
                    }
                }, 300);
            }
        });

        //Initialize the TTS engine
        tts = new TTS(this);

        //Samsung stuff
        samsungHelper = new SamsungHelper(this, prefs);
        //Initialize current capacitive buttons light
        samsungHelper.getButtonsLight();
        //Turn capacitive buttons lights off
        samsungHelper.setButtonsLight(OFF);
        //Stop service on home button press
        samsungHelper.setOnHomeButtonClickListener(new Runnable() {
            @Override
            public void run() {
                stoppedByShortcut = true;
                stopSelf();
            }
        });
    }

    private void setUpElements() {
        Typeface font = FontAdapter.getFontByNumber(this, prefs.font);

        clock = (Clock) mainView.findViewById(R.id.clock);
        clock.setStyle(this, prefs.clockStyle, prefs.textSize, prefs.textColor, prefs.showAmPm, font);

        LinearLayout batteryWrapper = (LinearLayout) mainView.findViewById(prefs.clockStyle != S7_DIGITAL ? R.id.battery_wrapper : R.id.s7_battery_wrapper);
        batteryView = (BatteryView) mainView.findViewById(R.id.battery);
        batteryView.init(this, clock.getDigitalS7(), prefs.batteryStyle, prefs.clockStyle == S7_DIGITAL, prefs.textColor, prefs.textSize, font);

        if (prefs.clockStyle == S7_DIGITAL) {
            prefs.dateStyle = DISABLED;
            prefs.batteryStyle = 1;
            mainView.removeView(dateView);
            mainView.removeView(batteryWrapper);
        }

        dateView = (DateView) mainView.findViewById(R.id.date);
        dateView.setDateStyle(prefs.dateStyle, prefs.textSize, prefs.textColor, font);
        TextView memoTV = (TextView) mainView.findViewById(R.id.memo_tv);
        if (!prefs.memoText.isEmpty()) {
            memoTV.setText(prefs.memoText);
            memoTV.setTypeface(font);
            memoTV.setTextColor(prefs.textColor);
            memoTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (prefs.memoTextSize));
        } else
            mainView.removeView(memoTV);
    }

    private void refresh() {
        Log.d(MAIN_SERVICE_LOG_TAG, "Refresh");
        if (clock.getAnalogClock() != null)
            clock.getAnalogClock().setTime(Calendar.getInstance());
        if (prefs.clockStyle == S7_DIGITAL)
            clock.getDigitalS7().update(prefs.showAmPm);

        refreshing = true;
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (Globals.isShown) refresh();
                        else refreshing = false;
                    }
                },
                6000);
    }

    private void refreshLong(boolean first) {
        Log.d(MAIN_SERVICE_LOG_TAG, "Long Refresh");
        if (!first && prefs.moveWidget != DISABLED)
            ViewUtils.move(this, mainView, prefs.moveWidget == MOVE_WITH_ANIMATION, prefs.orientation, dateView.isFull() || clock.isFull() || !prefs.memoText.isEmpty());
        String monthAndDayText = Utils.getDateText(this);
        dateView.update(monthAndDayText);
        if (prefs.clockStyle == S7_DIGITAL)
            clock.getDigitalS7().setDate(monthAndDayText);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (Globals.isShown) refreshLong(false);
                        else refreshing = false;
                    }
                },
                24000);
    }

    private void setLights(boolean state, boolean nightMode, boolean first) {
        if (first && state) {
            Log.d(MAIN_SERVICE_LOG_TAG, "Display turned on");
            mainView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        } else if (state) {
            boolean opaque = mainView.getAlpha() == 1f;
            if (nightMode && opaque) {
                AlphaAnimation alpha = new AlphaAnimation(1f, NIGHT_MODE_ALPHA);
                alpha.setDuration(android.R.integer.config_longAnimTime);
                mainView.startAnimation(alpha);
            } else if (!nightMode && !opaque) {
                AlphaAnimation alpha = new AlphaAnimation(NIGHT_MODE_ALPHA, 1f);
                alpha.setDuration(android.R.integer.config_longAnimTime);
                mainView.startAnimation(alpha);
            }
        }

        if (Utils.isAndroidNewerThanM())
            if (!System.canWrite(this)) {
                Log.d(MAIN_SERVICE_LOG_TAG, "Can't modify system settings");
                return;
            }
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, state ? (nightMode ? 0 : prefs.brightness) : originalBrightness);
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, state ? 0 : originalAutoBrightnessStatus);
        Log.d("Setting brightness to", String.valueOf(state ? (nightMode ? 0 : prefs.brightness) : originalBrightness));
    }

    private void unregisterUnlockReceiver() {
        try {
            unregisterReceiver(unlockReceiver);
        } catch (IllegalArgumentException ignored) {
            Log.d(MAIN_SERVICE_LOG_TAG, "Unlock receiver was not registered");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Globals.onNotificationAction = null;
        //Dismiss the app listener
        currentAppResolver.destroy();
        //Stop home button watcher
        samsungHelper.stopHomeWatcher();
        //Dismiss doze
        if (dozeManager != null)
            dozeManager.exitDoze();
        if (flashlight != null)
            flashlight.destroy();
        //Dismissing the wakelock holder
        stayAwakeWakeLock.release();
        if (proximityToTurnOff != null && proximityToTurnOff.isHeld())
            proximityToTurnOff.release();
        //Stopping tts
        tts.destroy();
        tts = null;
        //Unregister receivers
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        unregisterUnlockReceiver();
        batteryView.destroy();

        samsungHelper.setButtonsLight(ON);

        frameLayout.setOnTouchListener(null);
        if (clock.getTextClock() != null)
            clock.getTextClock().destroy(); //Kill the clock manually because the stock TextClock is kinda broken
        if (frameLayout.getWindowToken() != null) {
            if (prefs.exitAnimation == FADE_OUT && stoppedByShortcut) {
                Utils.Animations.fadeOutWithAction(frameLayout, new Runnable() {
                    @Override
                    public void run() {
                        if (frameLayout.getWindowToken() != null) {
                            windowManager.removeView(frameLayout);
                            setLights(OFF, false, false);
                        }
                    }
                });
            } else if (prefs.exitAnimation == SLIDE_OUT && stoppedByShortcut) {
                Utils.Animations.slideOutWithAction(frameLayout, -new DisplaySize(this).getHeight(prefs.orientation.equals(VERTICAL)), new Runnable() {
                    @Override
                    public void run() {
                        if (frameLayout.getWindowToken() != null) {
                            windowManager.removeView(frameLayout);
                            setLights(OFF, false, false);
                        }
                    }
                });
            } else {
                setLights(OFF, false, false);
                windowManager.removeView(frameLayout);
            }
        }

        Globals.isShown = false;
        Globals.isServiceRunning = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Globals.killedByDelay = false;
            }
        }, 15000);
        Log.d(MAIN_SERVICE_LOG_TAG, "Main service has stopped");
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY:
                if (event.values[0] < 1) {
                    // Sensor distance smaller than 1cm
                    stayAwakeWakeLock.release();
                    Globals.isShown = false;
                    Globals.sensorIsScreenOff = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (Shell.SU.available())
                                Shell.SU.run("input keyevent 26"); // Screen off using root
                            else
                                ((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow(); //Screen off using device admin
                        }
                    }).start();
                } else {
                    if (!Globals.sensorIsScreenOff) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onSensorChanged(event);
                            }
                        }, 200);
                        return;
                    }
                    ScreenReceiver.turnScreenOn(this, false);
                    Globals.isShown = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!refreshing) {
                                refresh();
                                refreshLong(true);
                            }
                            stayAwakeWakeLock.acquire();
                        }
                    }, 500);
                }
                break;
            case Sensor.TYPE_LIGHT:
                Log.d("Lights changed", String.valueOf(event.values[0]));
                setLights(ON, event.values[0] < 2, false);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class OnDismissListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;

        OnDismissListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 150;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!isInCenter(e1)) {
                    return false;
                }
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d(MAIN_SERVICE_LOG_TAG, "Swipe right");
                        } else {
                            Log.d(MAIN_SERVICE_LOG_TAG, "Swipe left");
                        }
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        Log.d(MAIN_SERVICE_LOG_TAG, "Swipe bottom");
                    } else {
                        Log.d(MAIN_SERVICE_LOG_TAG, "Swipe top");
                        return gestureAction(prefs.swipeAction);
                    }

                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return gestureAction(prefs.doubleTapAction);
            }

            private boolean isInCenter(MotionEvent e) {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                return e.getX() > width / 4 && e.getX() < width * 3 / 4 && e.getY() > height / 2.5 && e.getY() < height * 4 / 5;
            }
        }
    }

    private Flashlight flashlight;

    private boolean gestureAction(int gesture) {
        if (gesture == ACTION_UNLOCK) {
            stoppedByShortcut = true;
            stopSelf();
            return true;
        } else if (gesture == ACTION_SPEAK) {
            tts.sayCurrentStatus();
            return true;
        } else if (gesture == ACTION_FLASHLIGHT) {
            if (flashlight == null)
                flashlight = new Flashlight(this);
            if (!flashlight.isLoading())
                flashlight.toggle();
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
