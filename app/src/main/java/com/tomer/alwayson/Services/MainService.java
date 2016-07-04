package com.tomer.alwayson.Services;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.System;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.DAReceiver;
import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.Receivers.UnlockReceiver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service implements SensorEventListener, ContextConstatns, TextToSpeech.OnInitListener {

    private Prefs prefs;
    private int originalBrightness = 100;
    private int originalAutoBrightnessStatus;
    private TextView calendarTV, batteryTV;
    private ImageView batteryIV;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private View mainView;
    TextClock textClock;
    private LinearLayout iconWrapper;
    private PowerManager.WakeLock stayAwakeWakeLock;
    private UnlockReceiver unlockReceiver;
    private int originalCapacitiveButtonsState = 1500;
    private int height, width;

    private SensorManager sensorManager;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean charging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
            Log.d(MAIN_SERVICE_LOG_TAG, "Battery level " + level);
            Log.d(MAIN_SERVICE_LOG_TAG, "Battery charging " + charging);
            batteryTV.setText(String.valueOf(level) + "%");
            int res;
            if (charging)
                res = R.drawable.ic_battery_charging;
            else {
                if (level > 90)
                    res = R.drawable.ic_battery_full;
                else if (level > 70)
                    res = R.drawable.ic_battery_90;
                else if (level > 50)
                    res = R.drawable.ic_battery_60;
                else if (level > 30)
                    res = R.drawable.ic_battery_30;
                else if (level > 20)
                    res = R.drawable.ic_battery_20;
                else if (level > 0)
                    res = R.drawable.ic_battery_alert;
                else
                    res = R.drawable.ic_battery_unknown;
            }
            batteryIV.setImageResource(res);
        }
    };


    @SuppressWarnings("WeakerAccess")
    private double randInt(double min, double max) {
        return new Random().nextInt((int) ((max - min) + 1)) + min;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(getApplicationContext());
        prefs.apply();
        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        originalAutoBrightnessStatus = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCameraUsedByApp() && prefs.stopOnCamera) //Check if user just opened the camera, if so, dismiss
                    stopSelf();
            }
        }, 700);//Delay: Because it takes some time to start the camera on some devices

        // Setup UI
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams windowParams;
        if (Build.VERSION.SDK_INT < 19) {
            windowParams = new WindowManager.LayoutParams(-1, -1, 2010, 65794, -2);
        } else {
            windowParams = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
        }

        windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        frameLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    if (prefs.getStringByKey(VOLUME_KEYS, "off").equals("speak")) {
                        tts = new TextToSpeech(getApplicationContext(), MainService.this);
                        tts.setLanguage(Locale.getDefault());
                        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
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
                        tts = new TextToSpeech(getApplicationContext(), MainService.this);
                        tts.setLanguage(Locale.getDefault());
                        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
                        return true;
                    }
                    return false;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                    stopSelf();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        if (!prefs.getStringByKey(DOUBLE_TAP, "off").equals("off") || !prefs.getStringByKey(SWIPE_UP, "off").equals("off"))
            frameLayout.setOnTouchListener(new OnDismissListener(this));
        frameLayout.setBackgroundColor(Color.BLACK);
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mainView = layoutInflater.inflate(R.layout.clock_widget, frameLayout);
        textClock = (TextClock) mainView.findViewById(R.id.digital_clock);
        if (prefs.orientation.equals("horizontal"))//Setting screen orientation if horizontal
            windowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        setUpElements((LinearLayout) mainView.findViewById(R.id.watchface_wrapper), (LinearLayout) mainView.findViewById(R.id.clock_wrapper), (LinearLayout) mainView.findViewById(R.id.date_wrapper), (LinearLayout) mainView.findViewById(R.id.battery_wrapper));

        //Setting clock text color and size
        textClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.textSize);
        textClock.setTextColor(prefs.textColor);

        //Settings clock format
        if (!prefs.showAmPm)
            textClock.setFormat12Hour("h:mm");
        textClock.setTextLocale(getApplicationContext().getResources().getConfiguration().locale);

        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        if (!prefs.moveWidget) {
            mainLayoutParams.gravity = Gravity.CENTER;
        } else {
            mainLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        }
        mainView.setLayoutParams(mainLayoutParams);
        iconWrapper = (LinearLayout) mainView.findViewById(R.id.icons_wrapper);

        unlockReceiver = new UnlockReceiver();
        IntentFilter intentFilter = new IntentFilter();

        //Adding the intent from the pre-defined array filters
        for (String filter : Constants.unlockFilters) {
            intentFilter.addAction(filter);
        }
        registerReceiver(unlockReceiver, intentFilter);

        try {
            windowManager.addView(frameLayout, windowParams);
        } catch (Exception e) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        // Sensor handling
        if (prefs.proximityToLock || prefs.autoNightMode) //If any sensor is required
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //If proximity option is on, set it up
        if (prefs.proximityToLock) {
            Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mAdminName = new ComponentName(this, DAReceiver.class);
            if (proximitySensor != null && (Shell.SU.available() || (mDPM != null && mDPM.isAdminActive(mAdminName)))) {
                Log.d(MAIN_SERVICE_LOG_TAG, "STARTING PROXIMITY SENSOR");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    sensorManager.registerListener(this, proximitySensor, (int) TimeUnit.SECONDS.toMicros(6), 500000);
                else
                    sensorManager.registerListener(this, proximitySensor, (int) TimeUnit.SECONDS.toMicros(6));
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15), 500000);
                else
                    sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15));
            }
        }

        //Delay to stop
        if (Integer.parseInt(prefs.stopDelay) > 0) {
            final int delayInMilliseconds = Integer.parseInt(prefs.stopDelay) * 1000 * 60;
            Log.d(MAIN_SERVICE_LOG_TAG, "Setting delay to stop in minutes " + prefs.stopDelay);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
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
        startService(new Intent(getApplicationContext(), NotificationListener.class)); //Starting notification listener service
        refresh();
        refreshLong();

        //All Samsung's stuff
        if (!prefs.getBoolByKey(Prefs.KEYS.HAS_SOFT_KEYS.toString(), false)) {
            try {
                originalCapacitiveButtonsState = System.getInt(getContentResolver(), "button_key_light");
            } catch (Settings.SettingNotFoundException e) {
                Log.d(MAIN_SERVICE_LOG_TAG, "First method of getting the buttons status failed.");
                try {
                    originalCapacitiveButtonsState = (int) System.getLong(getContentResolver(), "button_key_light");
                } catch (Exception ignored) {
                    Log.d(MAIN_SERVICE_LOG_TAG, "Second method of getting the buttons status failed.");
                    try {
                        originalCapacitiveButtonsState = Settings.Secure.getInt(getContentResolver(), "button_key_light");
                    } catch (Exception ignored3) {
                        Log.d(MAIN_SERVICE_LOG_TAG, "Third method of getting the buttons status failed.");
                    }
                }
            }
        }

        //Turn capacitive buttons lights off
        setButtonsLight(true);

        //Turn lights on
        setLights(ON, false, true);

        //Turn screen on with delay
        stayAwakeWakeLock.acquire();
    }

    private boolean isCameraUsedByApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }

    private void setUpElements(LinearLayout watchfaceWrapper, LinearLayout clockWrapper, LinearLayout dateWrapper, LinearLayout batteryWrapper) {
        calendarTV = (TextView) dateWrapper.findViewById(R.id.date_tv);
        batteryIV = (ImageView) batteryWrapper.findViewById(R.id.battery_percentage_icon);
        batteryTV = (TextView) batteryWrapper.findViewById(R.id.battery_percentage_tv);
        switch (prefs.clockStyle) {
            case 0:
                watchfaceWrapper.removeView(clockWrapper);
                break;
            case 1:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.analog_clock));
                break;
            case 2:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                break;
        }
        switch (prefs.dateStyle) {
            case 0:
                watchfaceWrapper.removeView(dateWrapper);
                break;
            case 1:
                calendarTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (prefs.textSize / 5));
                calendarTV.setTextColor(prefs.textColor);
                break;
        }
        switch (prefs.batteryStyle) {
            case 0:
                watchfaceWrapper.removeView(batteryWrapper);
                break;
            case 1:
                batteryTV.setTextColor(prefs.textColor);
                batteryIV.setColorFilter(prefs.textColor, PorterDuff.Mode.SRC_ATOP);
                registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                break;
        }
        Typeface font = Typeface.DEFAULT;
        switch (prefs.font) {
            case 1:
                font = Typeface.DEFAULT_BOLD;
                break;
            case 2:
                font = Typeface.defaultFromStyle(Typeface.ITALIC);
                break;
            case 3:
                font = Typeface.SERIF;
                break;
            case 4:
                font = Typeface.SANS_SERIF;
                break;
            case 5:
                font = Typeface.MONOSPACE;
                break;
        }
        textClock.setTypeface(font);
        batteryTV.setTypeface(font);
        calendarTV.setTypeface(font);
    }

    private void refresh() {
        Log.d(MAIN_SERVICE_LOG_TAG, "Refresh");
        if (Globals.notificationChanged) {
            iconWrapper.removeAllViews();
            for (Map.Entry<String, Drawable> entry : Globals.notificationsDrawables.entrySet()) {
                Drawable drawable = entry.getValue();
                drawable.setColorFilter(prefs.textColor, PorterDuff.Mode.SRC_ATOP);
                ImageView icon = new ImageView(getApplicationContext());
                icon.setImageDrawable(drawable);
                icon.setColorFilter(prefs.textColor, PorterDuff.Mode.SRC_ATOP);
                FrameLayout.LayoutParams iconLayoutParams = new FrameLayout.LayoutParams(96, 96, Gravity.CENTER);
                icon.setPadding(12, 0, 12, 0);
                icon.setLayoutParams(iconLayoutParams);
                iconWrapper.addView(icon);
            }
            Globals.notificationChanged = false;
        }

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (Globals.isShown)
                            refresh();
                    }
                },
                6000);
    }

    private void refreshLong() {
        Log.d(MAIN_SERVICE_LOG_TAG, "Long Refresh");
        if (prefs.moveWidget) {
            if (prefs.orientation.equals("vertical"))
                mainView.setY((float) (height - randInt(height / 1.4, height * 1.4)));
            else
                mainView.setX((float) (width - randInt(width / 1.3, width * 1.3)));
        }
        if (prefs.dateStyle != 0) {
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date.getTime()).toUpperCase();
            String month = new SimpleDateFormat("MMMM").format(date.getTime()).toUpperCase();
            String currentDate = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
            calendarTV.setText(dayOfWeek + "," + " " + month + " " + currentDate);
        }

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (Globals.isShown)
                            refreshLong();
                    }
                },
                20000);
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

    private void openAppByPM(String pm) {
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(pm);
        startActivity(LaunchIntent);
    }

    @Override
    public void onDestroy() {
        toStopTTS = true;
        TextToSpeech tts = new TextToSpeech(getApplicationContext(), MainService.this);
        tts.setLanguage(Locale.getDefault());
        tts.speak("Text to say aloud", TextToSpeech.QUEUE_FLUSH, null);


        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        unregisterReceiver(unlockReceiver);
        if (prefs.batteryStyle != 0)
            unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
        setButtonsLight(false);
        setLights(OFF, false, false);
        try {
            windowManager.removeView(frameLayout);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
        }
        stayAwakeWakeLock.release();
        Globals.isShown = false;
    }

    private void setButtonsLight(boolean state) {
        if (!prefs.getBoolByKey(Prefs.KEYS.HAS_SOFT_KEYS.toString(), false)) {
            try {
                System.putInt(getContentResolver(), "button_key_light", state ? 0 : originalCapacitiveButtonsState);
            } catch (IllegalArgumentException e) {
                Log.d(MAIN_SERVICE_LOG_TAG, "First method of settings the buttons state failed.");
                try {
                    Runtime r = Runtime.getRuntime();
                    r.exec("echo" + (state ? 0 : originalCapacitiveButtonsState) + "> /system/class/leds/keyboard-backlight/brightness");
                } catch (IOException e1) {
                    Log.d(MAIN_SERVICE_LOG_TAG, "Second method of settings the buttons state failed.");
                    try {
                        System.putLong(getContentResolver(), "button_key_light", state ? 0 : originalCapacitiveButtonsState);
                    } catch (Exception ignored) {
                        Log.d(MAIN_SERVICE_LOG_TAG, "Third method of settings the buttons state failed.");
                        try {
                            Settings.Secure.putInt(getContentResolver(), "button_key_light", state ? 0 : originalCapacitiveButtonsState);
                        } catch (Exception ignored3) {
                            Log.d(MAIN_SERVICE_LOG_TAG, "Fourth method of settings the buttons state failed.");
                        }
                    }
                }
            }
            try {
                Intent i = new Intent();
                i.setComponent(new ComponentName("tomer.com.alwaysonamoledplugin", "tomer.com.alwaysonamoledplugin.CapacitiveButtons"));
                i.putExtra("state", state);
                i.putExtra("originalCapacitiveButtonsState", originalCapacitiveButtonsState);
                ComponentName c = startService(i);
                Log.d(MAIN_SERVICE_LOG_TAG, "Started plugin");
            } catch (Exception e) {
                Log.d(MAIN_SERVICE_LOG_TAG, "Fifth (plugin) method of settings the buttons state failed.");
                Toast.makeText(getApplicationContext(), getString(R.string.error_2_plugin_not_installed), Toast.LENGTH_LONG).show();
            }
        }
    }

    public float getBatteryLevel() {
        Intent batteryIntent = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    TextToSpeech tts;
    boolean toStopTTS;

    @Override
    public void onInit(int status) {
        if (toStopTTS) {
            try {
                tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null);
            } catch (NullPointerException ignored) {
            }
            return;
        }
        if (status == TextToSpeech.SUCCESS) {
            tts.speak("The time is " + (String) DateFormat.format("hh:mm aaa", Calendar.getInstance().getTime()), TextToSpeech.QUEUE_FLUSH, null);
            if (Globals.notificationsDrawables.size() > 0)
                tts.speak("You have " + Globals.notificationsDrawables.size() + " Notifications", TextToSpeech.QUEUE_ADD, null);
            tts.speak("Battery is at " + (int) getBatteryLevel() + " percent", TextToSpeech.QUEUE_ADD, null);
        }

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
                            tts = new TextToSpeech(getApplicationContext(), MainService.this);
                            tts.setLanguage(Locale.getDefault());
                            tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
                            tts.speak(String.valueOf(textClock.getText()), TextToSpeech.QUEUE_ADD, null);
                            return true;
                        }
                    }

                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isInCenter(e)) {
                    return false;
                }
                Log.d(MAIN_SERVICE_LOG_TAG, "Double tap" + prefs.getStringByKey(DOUBLE_TAP, ""));
                if (prefs.doubleTapToStop) {
                    stopSelf();
                    return true;
                }
                if (prefs.getStringByKey(DOUBLE_TAP, "unlock").equals("speak")) {
                    tts = new TextToSpeech(getApplicationContext(), MainService.this);
                    tts.setLanguage(Locale.getDefault());
                    tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
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
