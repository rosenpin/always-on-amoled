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
import com.tomer.alwayson.helpers.BatterySaver;
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

import static android.hardware.SensorManager.SENSOR_DELAY_UI;

public class MainService extends Service implements SensorEventListener, ContextConstatns {

    public static boolean stoppedByShortcut;
    public static boolean initialized;
    public static boolean isScreenOn;
    boolean firstRefresh = true;
    int refreshDelay = 12000;
    FrameLayout blackScreen;
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
    private MusicPlayer musicPlayer;
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
    private BatterySaver batterySaver;

    @Override
    public int onStartCommand(Intent origIntent, int flags, int startId) {
        if (windowParams == null) {
            windowParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 65794, -2);
            if (origIntent != null) {
                demo = origIntent.getBooleanExtra("demo", false);
                windowParams.type = origIntent.getBooleanExtra("demo", false) ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : Utils.isSamsung() ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            } else
                windowParams.type = Utils.isSamsung() ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
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
            samsungHelper.setOnHomeButtonClickListener(() -> {
                stoppedByShortcut = true;
                stopThis();
            });
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

        //Battery Saver
        if (prefs.batterySaver) {
            batterySaver = new BatterySaver(this);
            batterySaver.setSystemBatterySaver(true);
            prefs.brightness = prefs.brightness / 2;
            refreshDelay = refreshDelay * 2;
            prefs.moveWidget = MOVE_NO_ANIMATION;
            prefs.autoNightMode = false;
            prefs.stopOnCamera = false;
            prefs.stopOnGoogleNow = false;
            Utils.killBackgroundProcesses(this);
        }

        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        brightnessManager = new BrightnessManager(this);

        //Initialize stopped by shortcut
        stoppedByShortcut = false;

        // Setup UI
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        setTheme(R.style.AppTheme);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        frameLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN))
                    return gestureAction(prefs.volumeButtonsAction) || !prefs.disableVolumeKeys || !musicPlayer.isShown();
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    gestureAction(prefs.backButtonAction);
                return super.dispatchKeyEvent(event);
            }
        };
        if (prefs.doubleTapAction != ACTION_OFF_GESTURE || prefs.swipeUpAction != ACTION_OFF_GESTURE)
            frameLayout.setOnTouchListener(new OnDismissListener(this));
        frameLayout.setBackgroundColor(Color.BLACK);
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mainView = (LinearLayout) (layoutInflater.inflate(R.layout.clock_widget, frameLayout).findViewById(R.id.watchface_wrapper));
        iconsWrapper = (IconsWrapper) mainView.findViewById(R.id.icons_wrapper);
        musicPlayer = (MusicPlayer) mainView.findViewById(R.id.music_player);
        notificationsMessageBox = (MessageBox) mainView.findViewById(R.id.notifications_box);
        iconsWrapper.setMessageBox(notificationsMessageBox);
        setUpElements();

        FrameLayout.LayoutParams mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
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
        if (prefs.proximityToLock || prefs.autoNightMode)
            //If any sensor is required
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //If proximity option is on, set it up
        if (prefs.proximityToLock) {
            if (Utils.isAndroidNewerThanL() && !Utils.isSamsung()) {
                proximityToTurnOff = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, getPackageName() + " wakelock_holder");
                proximityToTurnOff.acquire();
            } else {
                Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (proximitySensor != null) {
                    Utils.logDebug(MAIN_SERVICE_LOG_TAG, "STARTING PROXIMITY SENSOR");
                    sensorManager.registerListener(this, proximitySensor, SENSOR_DELAY_UI, 1000000);
                }
            }
        }
        //If auto night mode option is on, set it up
        if (prefs.autoNightMode) {
            Sensor lightSensor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT, false);
            else
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
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
        else
            Utils.logInfo(MAIN_SERVICE_LOG_TAG, "Notifications are disabled");
        UIhandler = new Handler();
        refresh();

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
                    }
                },
                400);

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

        //Turn lights on
        setLights(ON, false, true);
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
                    if (clock != null) {
                        if (clock.getAnalogClock() != null)
                            clock.getAnalogClock().setTime(Calendar.getInstance());
                        if (prefs.clockStyle == S7_DIGITAL)
                            clock.getDigitalS7().update(prefs.showAmPm);
                    }
                });
                if (longRefresh[0])
                    longRefresh();
                longRefresh[0] = !longRefresh[0];
            }
        };
        refreshTimer.schedule(timerTask, 0L, refreshDelay);
    }

    private void longRefresh() {
        if (!firstRefresh && prefs.moveWidget != DISABLED)
            ViewUtils.move(getApplicationContext(), mainView, prefs.moveWidget == MOVE_WITH_ANIMATION, prefs.orientation, isBig());
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

    private boolean isBig() {
        return dateView.isFull() || clock.isFull() || !prefs.memoText.isEmpty() || musicPlayer.isShown();
    }

    private void setLights(boolean state, boolean nightMode, boolean first) {
        if (first && state) {
            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Display turned on");
            if (!isScreenOn) {
                new Handler().postDelayed(() -> {
                    //Turn on the display
                    if (!stayAwakeWakeLock.isHeld()) stayAwakeWakeLock.acquire();
                    mainView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                }, 300);
            }
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
        if (batterySaver != null)
            batterySaver.setSystemBatterySaver(batterySaver.originalBatterySaverMode);
        MainService.initialized = false;
        Globals.onNotificationAction = null;
        //Dismiss the app listener
        currentAppResolver.destroy();
        //Dismiss music player
        musicPlayer.destroy();
        //Dismiss doze
        if (dozeManager != null)
            dozeManager.exitDoze();
        if (flashlight != null)
            flashlight.destroy();
        //Dismissing the wakelock holder
        stayAwakeWakeLock.release();
        if (proximityToTurnOff != null && proximityToTurnOff.isHeld())
            proximityToTurnOff.release();
        showBlackScreen(false);
        //Stopping tts
        tts.destroy();
        tts = null;
        //Unregister receivers
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        unregisterUnlockReceiver();
        batteryView.destroy();

        samsungHelper.setButtonsLight(ON);
        samsungHelper.destroyHomeButtonListener(getApplication());

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
                Utils.logDebug("proximity", String.valueOf(event.values[0]));
                if (event.values[0] < 1) {
                    // Sensor distance smaller than 1cm
                    stayAwakeWakeLock.release();
                    Globals.isShown = false;
                    Globals.sensorIsScreenOff = false;
                    if (isScreenOn) {
                        showBlackScreen(true);
                        new Thread(() -> {
                            try {
                                if (Shell.SU.available())
                                    Shell.SU.run("input keyevent 26"); // Screen off using root
                            } catch (SecurityException e) {
                                ((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow(); //Screen off using device admin
                            }
                        }).start();
                    }
                } else {
                    showBlackScreen(false);
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

    private void showBlackScreen(boolean show) {
        if (blackScreen == null)
            blackScreen = new FrameLayout(this);
        blackScreen.setBackgroundColor(Color.BLACK);
        blackScreen.setForegroundGravity(Gravity.CENTER);
        try {
            if (show) {
                if (!blackScreen.isAttachedToWindow())
                    windowManager.addView(blackScreen, windowParams);
            } else if (blackScreen.isAttachedToWindow())
                windowManager.removeView(blackScreen);
        } catch (IllegalStateException ignored) {
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
            return true;
        }
        return false;
    }

    public void stopThis() {
        Utils.logDebug("Stopping service", "now");
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
        intent.putExtra("log", "Message: \n" + e.getMessage() + "\n\n" + "Error: \n" + e + "\n\n" + "Stack trace: \n" + Arrays.toString(e.getStackTrace()) + "\n\n" + "Cause: \n" + e.getCause() + "\n\n" + java.lang.System.err);
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
                try {
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
                                return gestureAction(prefs.swipeDownAction);
                            } else {
                                Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Swipe top");
                                return gestureAction(prefs.swipeUpAction);
                            }
                        }
                    }
                } catch (IllegalArgumentException ignored) {
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
