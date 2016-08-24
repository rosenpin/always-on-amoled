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
import android.widget.Toast;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;
import com.tomer.alwayson.activities.ReporterActivity;
import com.tomer.alwayson.helpers.BrightnessManager;
import com.tomer.alwayson.helpers.CurrentAppResolver;
import com.tomer.alwayson.helpers.DisplaySize;
import com.tomer.alwayson.helpers.DozeManager;
import com.tomer.alwayson.helpers.Flashlight;
import com.tomer.alwayson.helpers.GreenifyStarter;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.SamsungHelper;
import com.tomer.alwayson.helpers.TTS;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.helpers.ViewUtils;
import com.tomer.alwayson.receivers.ScreenReceiver;
import com.tomer.alwayson.receivers.UnlockReceiver;
import com.tomer.alwayson.views.BatteryView;
import com.tomer.alwayson.views.Clock;
import com.tomer.alwayson.views.DateView;
import com.tomer.alwayson.views.FontAdapter;
import com.tomer.alwayson.views.IconsWrapper;
import com.tomer.alwayson.views.MessageBox;
import com.tomer.alwayson.views.MusicPlayer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service implements SensorEventListener, ContextConstatns {

    public static boolean stoppedByShortcut;
    public static boolean initialized;
    public static boolean isScreenOn;
    boolean firstRefresh = true;
    private Timer refreshTimer;
    private boolean demo;
    private boolean refreshing = true;
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
    private Flashlight flashlight;
    private Handler UIhandler;
    private BrightnessManager brightnessManager;

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
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> handleUncaughtException(e));
        Globals.isServiceRunning = true;
        Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Main service has started");
        prefs = new Prefs(getApplicationContext());
        prefs.apply();
        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        brightnessManager = new BrightnessManager(this);
        //Check if screen is already on
        ScreenReceiver.updateScreenState(this);

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
        iconsWrapper.setMessageBox(notificationsMessageBox);
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
                    Utils.logDebug(MAIN_SERVICE_LOG_TAG, "STARTING PROXIMITY SENSOR");
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
                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "STARTING LIGHT SENSOR");
                sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15), 500000);
            }
        }

        //Delay to stop
        if (prefs.stopDelay > DISABLED) {
            final int delayInMilliseconds = prefs.stopDelay * 1000 * 60;
            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Setting delay to stop in minutes " + prefs.stopDelay);
            new Handler().postDelayed(() -> {
                stoppedByShortcut = true;
                stopThis();
                stayAwakeWakeLock.release();
                Globals.killedByDelay = true;
                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Stopping service after delay");
            }, delayInMilliseconds);
        }

        // UI refreshing
        Globals.notificationChanged = true; //Show notifications at first launch
        if (prefs.notificationsAlerts)
            //Starting the notification listener service
            startService(new Intent(getApplicationContext(), NotificationListener.class));
        UIhandler = new Handler();
        refresh();

        //Turn lights on
        setLights(ON, false, true);

        //Notification setup
        Globals.onNotificationAction = () -> {
            if (prefs.notificationsAlerts)
                UIhandler.post(() -> iconsWrapper.update(prefs.textColor, this::stopThis));
            if (Globals.newNotification != null && prefs.notificationPreview) {
                UIhandler.post(() -> notificationsMessageBox.showNotification(Globals.newNotification));
                notificationsMessageBox.setOnClickListener(view -> {
                    stoppedByShortcut = true;
                    if (notificationsMessageBox.getCurrentNotification().getIntent() != null) {
                        try {
                            notificationsMessageBox.getCurrentNotification().getIntent().send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        stoppedByShortcut = true;
                        stopThis();
                    }
                });
            }
        };
        Globals.onNotificationAction.run();

        //Turn screen on
        new Handler().postDelayed(
                () -> {
                    if (Globals.isServiceRunning) {
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
        currentAppResolver.executeForCurrentApp(() -> {
            Globals.waitingForApp = true;
            stopThis();
            new Handler().postDelayed(() -> Globals.waitingForApp = false, 300);
        });

        //Initialize the TTS engine
        tts = new TTS(this);

        //Samsung stuff
        samsungHelper = new SamsungHelper(this, prefs);
        //Initialize current capacitive buttons light
        samsungHelper.getButtonsLight();
        //Turn capacitive buttons lights off
        samsungHelper.setButtonsLight(OFF);
        MainService.initialized = true;
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
        final boolean[] longRefresh = {true};
        refreshTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Refresh");
                UIhandler.post(() -> {
                    if (clock.getAnalogClock() != null)
                        clock.getAnalogClock().setTime(Calendar.getInstance());
                    if (prefs.clockStyle == S7_DIGITAL)
                        clock.getDigitalS7().update(prefs.showAmPm);
                });
                if (longRefresh[0])
                    longRefresh();
                longRefresh[0] = !longRefresh[0];
            }
        };
        refreshTimer.schedule(timerTask, 0L, 12000);
    }

    private void longRefresh() {
        if (!firstRefresh && prefs.moveWidget != DISABLED)
            ViewUtils.move(getApplicationContext(), mainView, prefs.moveWidget == MOVE_WITH_ANIMATION, prefs.orientation, dateView.isFull() || clock.isFull() || !prefs.memoText.isEmpty());
        String monthAndDayText = Utils.getDateText(getApplicationContext());
        Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Long Refresh");
        UIhandler.post(() -> {
            dateView.update(monthAndDayText);
            if (prefs.clockStyle == S7_DIGITAL)
                clock.getDigitalS7().setDate(monthAndDayText);
        });
        if (firstRefresh)
            firstRefresh = false;
    }

    private void setLights(boolean state, boolean nightMode, boolean first) {
        if (first && state) {
            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Display turned on");
            if (!isScreenOn)
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
                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Can't modify system settings");
                return;
            }
        brightnessManager.setBrightness(state ? (nightMode ? 0 : prefs.brightness) : brightnessManager.getOriginalBrightness(), state ? 0 : brightnessManager.getOriginalBrightnessMode());
        Utils.logDebug("Setting brightness to", String.valueOf(state ? (nightMode ? 0 : prefs.brightness) : brightnessManager.getOriginalBrightness()));
    }

    private void unregisterUnlockReceiver() {
        try {
            unregisterReceiver(unlockReceiver);
        } catch (IllegalArgumentException ignored) {
            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Unlock receiver was not registered");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainService.initialized = false;
        Globals.onNotificationAction = null;
        //Dismiss the app listener
        currentAppResolver.destroy();
        //Dismiss music player
        ((MusicPlayer)mainView.findViewById(R.id.music_player)).destroy();
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
                Utils.Animations.fadeOutWithAction(frameLayout, () -> {
                    if (frameLayout.getWindowToken() != null) {
                        windowManager.removeView(frameLayout);
                        setLights(OFF, false, false);
                    }
                });
            } else if (prefs.exitAnimation == SLIDE_OUT && stoppedByShortcut) {
                Utils.Animations.slideOutWithAction(frameLayout, -new DisplaySize(this).getHeight(prefs.orientation.equals(VERTICAL)), () -> {
                    if (frameLayout.getWindowToken() != null) {
                        windowManager.removeView(frameLayout);
                        setLights(OFF, false, false);
                    }
                });
            } else {
                setLights(OFF, false, false);
                windowManager.removeView(frameLayout);
            }
        }

        refreshTimer.cancel();

        Globals.isShown = false;
        Globals.isServiceRunning = false;
        new Handler().postDelayed(() -> Globals.killedByDelay = false, 15000);
        Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Main service has stopped");
        Thread.setDefaultUncaughtExceptionHandler(null);
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
                    new Thread(() -> {
                        if (Shell.SU.available())
                            Shell.SU.run("input keyevent 26"); // Screen off using root
                        else
                            ((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow(); //Screen off using device admin
                    }).start();
                } else {
                    if (!Globals.sensorIsScreenOff) {
                        new Handler().postDelayed(() -> onSensorChanged(event), 200);
                        return;
                    }
                    if (!isScreenOn)
                        ScreenReceiver.turnScreenOn(this, false);
                    Globals.isShown = true;
                    new Handler().postDelayed(() -> {
                        if (!refreshing)
                            refresh();
                        stayAwakeWakeLock.acquire();
                    }, 500);
                }
                break;
            case Sensor.TYPE_LIGHT:
                Utils.logDebug("Lights changed", String.valueOf(event.values[0]));
                setLights(ON, event.values[0] < 2, false);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean gestureAction(int gesture) {
        if (gesture == ACTION_UNLOCK) {
            stoppedByShortcut = true;
            stopThis();
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

    public void stopThis() {
        if (MainService.initialized)
            stopSelf();
        else
            Utils.logDebug(MainService.class.getSimpleName(), "Shouldn't kill the service: service wasn't initialized correctly");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void handleUncaughtException(Throwable e) {
        int reportNotificationID = 53;
        Context context = getApplicationContext();
        e.printStackTrace();
        Toast.makeText(context, R.string.error_0_unknown_error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, ReporterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        e.printStackTrace();
        intent.putExtra("log", "Message: " + e.getMessage() + "\n\n" + "Error:" + e + "\n\n" + "Stack trace:" + Arrays.toString(e.getStackTrace()) + "\n\n" + "Cause:" + e.getCause() + "\n\n" + java.lang.System.err);
        PendingIntent reportIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Utils.showErrorNotification(context, context.getString(R.string.error), context.getString(R.string.error_0_unknown_error_report_prompt), reportNotificationID, reportIntent);
        java.lang.System.exit(0);
        startService(new Intent(getApplicationContext(), StarterService.class));
        setLights(OFF, false, false);
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
                if (e2 != null) {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Swipe right");
                            } else {
                                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Swipe left");
                            }
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Swipe bottom");
                        } else {
                            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Swipe top");
                            return gestureAction(prefs.swipeAction);
                        }

                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return gestureAction(prefs.doubleTapAction);
            }

            private boolean isInCenter(MotionEvent e) {
                if (e != null) {
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    return e.getX() > width / 4 && e.getX() < width * 3 / 4 && e.getY() > height / 2.5 && e.getY() < height * 4 / 5;
                }
                return false;
            }
        }
    }
}
