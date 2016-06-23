package com.tomer.alwayson.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
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
import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.Receivers.UnlockReceiver;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;

public class MainService extends Service implements SensorEventListener, ContextConstatns {

    private Prefs prefs;
    private int originalBrightness = 100;
    private boolean proximityToLock;
    private int originalAutoBrightnessStatus;
    private TextView calendarTV, batteryTV;
    private AppCompatImageView batteryIV;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private View mainView;
    private LinearLayout iconWrapper;
    private PowerManager.WakeLock stayAwakeWakeLock;
    private UnlockReceiver unlockReceiver;

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
    public static double randInt(double min, double max) {
        return new Random().nextInt((int) ((max - min) + 1)) + min;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(getApplicationContext());
        prefs.apply();

        stayAwakeWakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, WAKE_LOCK_TAG);
        stayAwakeWakeLock.setReferenceCounted(false);
        originalAutoBrightnessStatus = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);

        if (prefs.notificationsAlerts && !isNotificationServiceRunning()) // Only start the service if it's not already running
            new Intent(getApplicationContext(), NotificationListener.class);

        // Setup UI

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stayAwakeWakeLock.acquire();
            }
        }, 500);

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
                    if (prefs.volumeToStop) {
                        stopSelf();
                        return true;
                    } else return prefs.disableVolumeKeys;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && prefs.backButtonToStop) {
                    stopSelf();
                    return true;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                    stopSelf();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        frameLayout.setOnTouchListener(new OnDismissListener(this));
        frameLayout.setBackgroundColor(Color.BLACK);
        frameLayout.setForegroundGravity(Gravity.CENTER);
        mainView = layoutInflater.inflate(R.layout.clock_widget, frameLayout);
        LinearLayout watchFaceWrapper = (LinearLayout) mainView.findViewById(R.id.watchface_wrapper);
        TextClock textClock = (TextClock) mainView.findViewById(R.id.time_tv);
        calendarTV = (TextView) mainView.findViewById(R.id.date_tv);
        batteryIV = (AppCompatImageView) mainView.findViewById(R.id.battery_percentage_icon);
        batteryTV = (TextView) mainView.findViewById(R.id.battery_percentage_tv);
        if (!prefs.showTime)
            watchFaceWrapper.removeView(textClock);
        if (!prefs.showDate)
            watchFaceWrapper.removeView(calendarTV);
        else {
            calendarTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (prefs.textSize / 5));
            calendarTV.setTextColor(prefs.textColor);
        }
        if (!prefs.showBattery)
            watchFaceWrapper.removeView(mainView.findViewById(R.id.battery_wrapper));
        else {
            batteryTV.setTextColor(prefs.textColor);
            batteryIV.setColorFilter(prefs.textColor, PorterDuff.Mode.SRC_ATOP);
            registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        textClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.textSize);
        textClock.setTextColor(prefs.textColor);
        if (!prefs.showAmPm)
            textClock.setFormat12Hour("K:m");
        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        if (!prefs.moveWidget) {
            mainLayoutParams.gravity = Gravity.CENTER;
        } else {
            refreshLong();
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor lightSensor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT, false);
        } else {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        proximityToLock = prefs.proximityToLock && Shell.SU.available();
        if (proximitySensor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                sensorManager.registerListener(this, proximitySensor, (int) TimeUnit.MILLISECONDS.toMicros(400), 100000);
            else
                sensorManager.registerListener(this, proximitySensor, (int) TimeUnit.MILLISECONDS.toMicros(400));
        }
        if (lightSensor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15), 500000);
            else
                sensorManager.registerListener(this, lightSensor, (int) TimeUnit.SECONDS.toMicros(15));
        }
        setLights(ON, false, true);

        // UI refreshing
        refresh();
    }

    private void refresh() {
        prefs.apply();

        if (prefs.showDate) {
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date.getTime()).toUpperCase();
            String month = new SimpleDateFormat("MMMM").format(date.getTime()).toUpperCase();
            String currentDate = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
            calendarTV.setText(dayOfWeek + "," + " " + month + " " + currentDate);
        }

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

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        refresh();
                    }
                },
                3000);
    }

    private void refreshLong() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        mainView.setY((float) (height - randInt(height / 1.4, height * 1.4)));

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
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

        /*
//        ToDo: Find a way to start this intent without the main activity getting started in the background
        Intent intent = new Intent(getApplicationContext(), DummyCapacitiveButtonsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("turn", !state);
        startActivity(intent);*/
        try {
            Settings.System.putInt(getContentResolver(), "button_key_light", state ? 0 : -1);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        unregisterReceiver(unlockReceiver);
        if (prefs.showBattery)
            unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
        setLights(OFF, false, false);
        try {
            windowManager.removeView(frameLayout);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
        }
        stayAwakeWakeLock.release();
        Globals.isShown = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY:
                if (proximityToLock) {
                    if (event.values[0] < 1) {
                        // Sensor distance smaller than 1cm
                        stayAwakeWakeLock.release();
                        Globals.isShown = false;
                        Globals.sensorIsScreenOff = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (Shell.SU.available())
                                    Shell.SU.run("input keyevent 26"); // Screen off
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

    private boolean isNotificationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationListener.class.getName().equals(service.service.getClassName())) {
                Log.d(NOTIFICATION_LISTENER_TAG, "Is already running");
                return true;
            }
        }
        Log.d(NOTIFICATION_LISTENER_TAG, "Is not running");
        return false;
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
                    }

                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isInCenter(e)) {
                    return false;
                }
                Log.d(MAIN_SERVICE_LOG_TAG, "Double tap");
                if (prefs.touchToStop) {
                    stopSelf();
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