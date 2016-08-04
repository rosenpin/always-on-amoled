package com.tomer.alwayson.Services;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Helpers.CurrentAppResolver;
import com.tomer.alwayson.Helpers.DozeManager;
import com.tomer.alwayson.Helpers.Prefs;
import com.tomer.alwayson.Helpers.SamsungHelper;
import com.tomer.alwayson.Helpers.TTS;
import com.tomer.alwayson.Helpers.Utils;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.BatteryReceiver;
import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.Receivers.UnlockReceiver;
import com.tomer.alwayson.Views.DigitalS7;
import com.tomer.alwayson.Views.FontAdapter;
import com.tomer.alwayson.Views.IconsWrapper;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service implements SensorEventListener, ContextConstatns {
    private boolean demo;
    private boolean refreshing;
    private int originalBrightness = 100;
    private int originalAutoBrightnessStatus;
    private int height, width;
    private BatteryReceiver batteryReceiver;
    private SamsungHelper samsungHelper;
    private DozeManager dozeManager;
    private TTS tts;
    private DigitalS7 digitalS7;
    private Prefs prefs;
    private TextView calendarTV;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private LinearLayout mainView;
    private WindowManager.LayoutParams windowParams;
    private PowerManager.WakeLock stayAwakeWakeLock;
    private UnlockReceiver unlockReceiver;
    private CustomAnalogClock analog24HClock;
    private IconsWrapper iconsWrapper;
    private PowerManager.WakeLock proximityToTurnOff;
    private SensorManager sensorManager;
    private CurrentAppResolver currentAppResolver;

    @Override
    public int onStartCommand(Intent origIntent, int flags, int startId) {
        if (windowParams == null) {
            windowParams = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
            if (origIntent != null) {
                demo = origIntent.getBooleanExtra("demo", false);
                windowParams.type = origIntent.getBooleanExtra("demo", false) ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            } else
                windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            if (prefs.orientation.equals("horizontal"))//Setting screen orientation if horizontal
                windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            try {
                windowManager.addView(frameLayout, windowParams);
            } catch (Exception e) {
                e.printStackTrace();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
        return super.onStartCommand(origIntent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Globals.waitingForApp) {
            Log.e(MAIN_SERVICE_LOG_TAG, "Waiting for app, killing service");
            stopSelf();
        }
        Globals.isServiceRunning = true;
        Log.d(MAIN_SERVICE_LOG_TAG, "Main service has started");
        prefs = new Prefs(getApplicationContext());
        prefs.apply();
        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        originalAutoBrightnessStatus = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 100);

        // Setup UI
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        setTheme(R.style.AppTheme);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        frameLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    if (prefs.getStringByKey(VOLUME_KEYS, "off").equals("speak")) {
                        tts.sayCurrentStatus();
                        return true;
                    } else if (prefs.volumeToStop) {
                        stopSelf();
                        return true;
                    } else return prefs.disableVolumeKeys;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (prefs.backButtonToStop)
                        stopSelf();
                    if (prefs.getStringByKey(BACK_BUTTON, "off").equals("speak")) {
                        tts.sayCurrentStatus();
                    }
                    return false;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        if (!prefs.getStringByKey(DOUBLE_TAP, "off").equals("off") || !prefs.getStringByKey(SWIPE_UP, "off").equals("off"))
            frameLayout.setOnTouchListener(new OnDismissListener(this));
        frameLayout.setBackgroundColor(Color.BLACK);
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mainView = (LinearLayout) (layoutInflater.inflate(prefs.orientation.equals("vertical") ? R.layout.clock_widget : R.layout.clock_widget_horizontal, frameLayout).findViewById(R.id.watchface_wrapper));
        setUpElements();

        FrameLayout.LayoutParams mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mainLayoutParams.gravity = prefs.moveWidget == DISABLED ? Gravity.CENTER : Gravity.CENTER_HORIZONTAL;

        mainView.setLayoutParams(mainLayoutParams);
        iconsWrapper = (IconsWrapper) mainView.findViewById(R.id.icons_wrapper);

        unlockReceiver = new UnlockReceiver();
        IntentFilter intentFilter = new IntentFilter();

        //Adding the intent from the pre-defined array filters
        for (String filter : Constants.unlockFilters) {
            intentFilter.addAction(filter);
        }
        try {
            unregisterReceiver(unlockReceiver);
        } catch (Exception ignored) {
        }
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
                    stopSelf();
                    stayAwakeWakeLock.release();
                    Globals.killedByDelay = true;
                    Log.d(MAIN_SERVICE_LOG_TAG, "Stopping service after delay");
                }
            }, delayInMilliseconds);
        }

        //Finding height and width of screen to later move the display
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = prefs.orientation.equals("vertical") ? size.y : size.x;
        width = prefs.orientation.equals("vertical") ? size.x : size.y;

        // UI refreshing
        Globals.notificationChanged = true; //Show notifications at first launch
        if (prefs.notificationsAlerts)
            startService(new Intent(getApplicationContext(), NotificationListener.class)); //Starting notification listener service
        refresh();
        refreshLong(true);

        //Turn lights on
        setLights(ON, false, true);

        //Turn screen on
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        //Greenify integration
                        if (!demo)
                            if (Utils.isPackageInstalled(getApplicationContext(), "com.oasisfeng.greenify")) {
                                Intent i = new Intent();
                                i.setComponent(new ComponentName("com.oasisfeng.greenify", "com.oasisfeng.greenify.GreenifyShortcut"));
                                i.putExtra("noop-toast", true);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        //Turn on the display
                        if (!stayAwakeWakeLock.isHeld()) stayAwakeWakeLock.acquire();
                    }
                },
                500);

        //Initializing Doze
        if (prefs.getBoolByKey("doze_mode", false)) {
            dozeManager = new DozeManager(this);
            dozeManager.enterDoze();
        }

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
                }, 500);
            }
        });

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
                stopSelf();
            }
        });
    }

    private void setUpElements() {
        Log.d("Font to apply ", String.valueOf(prefs.font));
        Typeface font = FontAdapter.getFontByNumber(this, prefs.font);
        LinearLayout dateWrapper = (LinearLayout) mainView.findViewById(R.id.date_wrapper);
        LinearLayout batteryWrapper = (LinearLayout) mainView.findViewById(prefs.clockStyle != S7_DIGITAL ? R.id.battery_wrapper : R.id.s7_battery_wrapper);
        LinearLayout clockWrapper = (LinearLayout) mainView.findViewById(R.id.clock_wrapper);
        calendarTV = (TextView) dateWrapper.findViewById(R.id.date_tv);
        ImageView batteryIV = (ImageView) batteryWrapper.findViewById(R.id.battery_percentage_icon);
        TextView batteryTV = (TextView) batteryWrapper.findViewById(R.id.battery_percentage_tv);
        ViewGroup.LayoutParams lp = clockWrapper.findViewById(R.id.custom_analog_clock).getLayoutParams();
        float clockSize = prefs.textSize < 80 ? prefs.textSize : 80;
        lp.height = (int) (clockSize * 10);
        lp.width = (int) (clockSize * 9.5);

        if (prefs.clockStyle >= ANALOG_CLOCK)
            analog24HClock = (CustomAnalogClock) clockWrapper.findViewById(R.id.custom_analog_clock);

        switch (prefs.clockStyle) {
            case DISABLED:
                mainView.removeView(clockWrapper);
                break;
            case DIGITAL_CLOCK:
                TextClock textClock = (TextClock) clockWrapper.findViewById(R.id.digital_clock);
                textClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.textSize);
                textClock.setTextColor(prefs.textColor);
                if (!prefs.showAmPm)
                    textClock.setFormat12Hour("h:mm");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textClock.setTextLocale(getApplicationContext().getResources().getConfiguration().getLocales().get(0));
                } else {
                    textClock.setTextLocale(getApplicationContext().getResources().getConfiguration().locale);
                }
                textClock.setTypeface(font);

                clockWrapper.removeView(clockWrapper.findViewById(R.id.custom_analog_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                break;
            case ANALOG_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.default_face, R.drawable.default_hour_hand, R.drawable.default_minute_hand, 225, false, false);
                break;
            case ANALOG24_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.clock_face, R.drawable.hour_hand, R.drawable.minute_hand, 0, true, false);
                break;
            case S7_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.s7_face, R.drawable.s7_hour_hand, R.drawable.s7_minute_hand, 0, false, false);
                break;
            case PEBBLE_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.pebble_face, R.drawable.pebble_hour_hand, R.drawable.pebble_minute_hand, 225, false, true);
                break;
            case S7_DIGITAL:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.custom_analog_clock));
                if (prefs.textSize > 90)
                    prefs.textSize = 90;
                digitalS7 = (DigitalS7) mainView.findViewById(R.id.s7_digital);
                digitalS7.init(font, prefs.textSize);
                prefs.dateStyle = DISABLED;
                prefs.batteryStyle = 1;
                mainView.removeView(dateWrapper);
                mainView.removeView(batteryWrapper);
                break;
            case FLAT_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.flat_face, R.drawable.flat_hour_hand, R.drawable.flat_minute_hand, 235, false, false);
                break;
            case FLAT_RED_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.flat_face, R.drawable.flat_red_hour_hand, R.drawable.flat_red_minute_hand, 0, false, false);
                break;
            case FLAT_STANDARD_TICKS:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analog24HClock.init(this, R.drawable.standard_ticks_face, R.drawable.hour_hand, R.drawable.minute_hand, 0, false, false);
                break;
        }
        switch (prefs.batteryStyle) {
            case 0:
                mainView.removeView(batteryWrapper);
                break;
            case 1:
                if (prefs.clockStyle != S7_DIGITAL) {
                    batteryTV.setTextColor(prefs.textColor);
                    batteryIV.setColorFilter(prefs.textColor, PorterDuff.Mode.SRC_ATOP);

                    batteryTV.setTypeface(font);
                    batteryTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (prefs.textSize * 0.2 * 1));
                    ViewGroup.LayoutParams batteryIVlp = batteryIV.getLayoutParams();
                    batteryIVlp.height = (int) (prefs.textSize);
                    batteryIV.setLayoutParams(batteryIVlp);
                }
                batteryReceiver = new BatteryReceiver(prefs.clockStyle == S7_DIGITAL ? digitalS7.getBatteryTV() : batteryTV, prefs.clockStyle == S7_DIGITAL ? digitalS7.getBatteryIV() : batteryIV);
                registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                break;
        }
        switch (prefs.dateStyle) {
            case DISABLED:
                mainView.removeView(dateWrapper);
                break;
            case 1:
                calendarTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (prefs.textSize / 5));
                calendarTV.setTextColor(prefs.textColor);
                calendarTV.setTypeface(font);
                break;
        }
        TextView memoTV = (TextView) mainView.findViewById(R.id.memo_tv);
        if (!prefs.memoText.isEmpty()) {
            memoTV.setText(prefs.memoText);
            memoTV.setTypeface(font);
            memoTV.setTextColor(prefs.textColor);
            memoTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (prefs.memoTextSize));
        } else
            mainView.removeView(memoTV);
        Log.d("Date", String.valueOf(prefs.dateStyle));
    }

    private void refresh() {
        Log.d(MAIN_SERVICE_LOG_TAG, "Refresh");
        iconsWrapper.update(prefs.notificationsAlerts, prefs.textColor);
        if (Globals.newNotification != null && prefs.notificationPreview)
            showMessage(Globals.newNotification);
        if (analog24HClock != null)
            analog24HClock.setTime(Calendar.getInstance());
        if (prefs.clockStyle == S7_DIGITAL)
            digitalS7.update(prefs.showAmPm);

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
        if (!first) {
            switch (prefs.moveWidget) {
                case MOVE_NO_ANIMATION:
                    if (prefs.orientation.equals("vertical"))
                        mainView.setY((float) (height - Utils.randInt(height / 2.1, height)));
                    else
                        mainView.setX((float) (width - Utils.randInt(width / 1.3, width * 1.3)));
                    break;
                case MOVE_WITH_ANIMATION:
                    if (prefs.orientation.equals("vertical"))
                        mainView.animate().translationY((float) (height - Utils.randInt(height / 2.1, height * 0.9))).setDuration(2000).setInterpolator(new FastOutSlowInInterpolator());
                    else
                        mainView.animate().translationX((float) (width - Utils.randInt(width / 1.3, width * 1.3))).setDuration(2000).setInterpolator(new FastOutSlowInInterpolator());
                    break;
            }
        }
        String monthAndDayText = DateUtils.formatDateTime(this, Calendar.getInstance().getTime().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY);
        if (prefs.dateStyle != DISABLED)
            calendarTV.setText(monthAndDayText);
        if (prefs.clockStyle == S7_DIGITAL)
            digitalS7.updateDate(monthAndDayText);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (Globals.isShown) refreshLong(false);
                        else refreshing = false;
                    }
                },
                16000);
    }

    private void setLights(boolean state, boolean nightMode, boolean first) {
        try {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, state ? Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL : originalAutoBrightnessStatus);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, state ? (nightMode ? 0 : prefs.brightness) : originalBrightness);
        } catch (Exception e) {
            Toast.makeText(MainService.this, getString(R.string.warning_3_allow_system_modification), Toast.LENGTH_SHORT).show();
        }

        if (state && mainView != null) {
            AlphaAnimation old = (AlphaAnimation) mainView.getAnimation();
            if (old != null && first) {
                mainView.clearAnimation();
                // Finish old animation
                try {
                    Field f = old.getClass().getField("mToAlpha");
                    f.setAccessible(true);
                    mainView.setAlpha(f.getFloat(old));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            boolean opaque = mainView.getAlpha() == 1f;
            if (nightMode && opaque) {
                AlphaAnimation alpha = new AlphaAnimation(1f, NIGHT_MODE_ALPHA);
                alpha.setDuration(400);
                mainView.startAnimation(alpha);
            } else if (!nightMode && !opaque) {
                AlphaAnimation alpha = new AlphaAnimation(NIGHT_MODE_ALPHA, 1f);
                alpha.setDuration(400);
                mainView.startAnimation(alpha);
            }
        }
    }

    private void showMessage(final NotificationListener.NotificationHolder notification) {
        if (!notification.getTitle().equals("null")) {
            //Clear previous animation
            if (mainView.findViewById(R.id.message_box).getAnimation() != null)
                mainView.findViewById(R.id.message_box).clearAnimation();
            //Fade in animation
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(1000);
            //Fade out animation
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(40000);
            fadeOut.setDuration(1000);
            //Set the notification text and icon
            ((TextView) mainView.findViewById(R.id.message_box).findViewById(R.id.message_box_title)).setText(notification.getTitle());
            ((TextView) mainView.findViewById(R.id.message_box).findViewById(R.id.message_box_message)).setText(notification.getMessage());
            ((ImageView) mainView.findViewById(R.id.message_box).findViewById(R.id.message_box_icon)).setImageDrawable(notification.getIcon());
            ((TextView) mainView.findViewById(R.id.message_box).findViewById(R.id.message_app_name)).setText(notification.getAppName());

            Globals.newNotification = null;
            //Run animations
            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            animation.addAnimation(fadeOut);
            mainView.findViewById(R.id.message_box).setAnimation(animation);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Dismiss the app listener
        currentAppResolver.destroy();
        //Stop home button watcher
        samsungHelper.stopHomeWatcher();
        //Dismiss doze
        if (dozeManager != null)
            dozeManager.exitDoze();
        //Dismissing the wakelock holder
        stayAwakeWakeLock.release();
        if (proximityToTurnOff != null && proximityToTurnOff.isHeld())
            proximityToTurnOff.release();
        Log.d(MAIN_SERVICE_LOG_TAG, "Main service has stopped");
        //Stopping tts
        tts.destroy();
        tts = null;
        //Unregister receivers
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        unregisterReceiver(unlockReceiver);
        if (prefs.batteryStyle != 0)
            unregisterReceiver(batteryReceiver);

        samsungHelper.setButtonsLight(ON);
        setLights(OFF, false, false);
        try {
            windowManager.removeView(frameLayout);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_0_unknown_error), Toast.LENGTH_SHORT).show();
        }
        Globals.isShown = false;
        Globals.isServiceRunning = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Globals.killedByDelay = false;
            }
        }, 15000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY:
                if (event.values[0] < 1) {
                    // Sensor distance smaller than 1cm
                    stayAwakeWakeLock.release();
                    Log.d("Proximity distance", String.valueOf(event.values[0]));
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
                        if (prefs.swipeToStop) {
                            stopSelf();
                            return true;
                        }
                        if (prefs.getStringByKey(SWIPE_UP, "off").equals("speak")) {
                            tts.sayCurrentStatus();
                            return true;
                        }
                    }

                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(MAIN_SERVICE_LOG_TAG, "Double tap" + prefs.getStringByKey(DOUBLE_TAP, ""));
                if (prefs.doubleTapToStop) {
                    stopSelf();
                    return true;
                }
                if (prefs.getStringByKey(DOUBLE_TAP, "unlock").equals("speak")) {
                    tts.sayCurrentStatus();
                    return true;
                }
                return false;
            }

            private boolean isInCenter(MotionEvent e) {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                return e.getX() > width / 4 && e.getX() < width * 3 / 4 && e.getY() > height / 2.5 && e.getY() < height * 4 / 5;
            }
        }
    }
}
