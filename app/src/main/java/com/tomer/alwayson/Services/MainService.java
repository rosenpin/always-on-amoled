package com.tomer.alwayson.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tomer.alwayson.Activities.DummyBrightnessActivity;
import com.tomer.alwayson.Constants;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.UnlockReceiver;

import java.util.Map;
import java.util.Random;

public class MainService extends Service {

    private static final String LOG_TAG = MainService.class.getSimpleName();
    private static final String NOTIFICATION_LOG_TAG = NotificationListener.class.getSimpleName();

    private Prefs prefs;
    private FrameLayout frameLayout;
    private View mainView;
    private LinearLayout iconWrapper;
    private int originalBrightness = 180;
    private int originalAutoBrightnessStatus;
    private PowerManager.WakeLock WakeLock1;
    private UnlockReceiver unlockReceiver;

    @SuppressWarnings("WeakerAccess")
    public static double randInt(double min, double max) {
        double random = new Random().nextInt((int) ((max - min) + 1)) + min;
        return random;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(getApplicationContext());
        prefs.apply();

        if (prefs.notificationsAlerts && !isNotificationServiceRunning())//Only start the service if it's not already running
            new Intent(getApplicationContext(), NotificationListener.class);

        setBrightness(prefs.brightness, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        WindowManager.LayoutParams lp;

        if (Build.VERSION.SDK_INT < 19) {
            lp = new WindowManager.LayoutParams(-1, -1, 2010, 65794, -2);
        } else {
            lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
        }

        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        frameLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    if (prefs.volumeToStop) {
                        stopSelf();
                        return true;
                    } else {
                        return true;
                    }
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
        mainView = layoutInflater.inflate(R.layout.clock_widget, frameLayout);
        iconWrapper = (LinearLayout) mainView.findViewById(R.id.icons_wrapper);
        unlockReceiver = new UnlockReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_ASSIST);
        filter.addAction(Intent.ACTION_ALL_APPS);
        registerReceiver(unlockReceiver, filter);

        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        if (!prefs.moveWidget) {
            mainLayoutParams.gravity = Gravity.CENTER;
        } else {
            refreshLong();
            mainLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        }

        mainView.setLayoutParams(mainLayoutParams);

        frameLayout.setBackgroundColor(Color.BLACK);

        frameLayout.setOnTouchListener(new OnDismissListener(this));

        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(frameLayout, lp);
        } catch (Exception e) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        refresh();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        WakeLock1 = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, "WAKEUP");
                        WakeLock1.acquire();
                    }
                },
                500);

        frameLayout.setForegroundGravity(Gravity.CENTER);

        disableButtonBacklight();
    }

    private void disableButtonBacklight() {
        /*Intent intent = new Intent(getApplicationContext(), DummyCapacitiveButtonsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("turn",false);
        startActivity(intent);*/
        try {
            Settings.System.putInt(getContentResolver(), "button_key_light", 0);
        } catch (Exception ignored) {
        }
    }

    private void enableButtonBacklight() {
        /*
        Intent intent = new Intent(getApplicationContext(), DummyCapacitiveButtonsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("turn",true);
        startActivity(intent);*/
        try {
            Settings.System.putInt(getContentResolver(), "button_key_light", -1);
        } catch (Exception ignored) {
        }
    }

    private void setBrightness(int brightnessVal, int autoBrightnessStatusVar) {
        originalAutoBrightnessStatus = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 180);
        Log.d(LOG_TAG, String.format("Original brightness: %1$s", originalBrightness));
        try {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, autoBrightnessStatusVar);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessVal);

            Intent intent = new Intent(getBaseContext(), DummyBrightnessActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("brightness value", brightnessVal);
            // getApplication().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(MainService.this, getString(R.string.warning_3_allow_system_modification), Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {

        iconWrapper.removeAllViews();
        for (Map.Entry<String, Drawable> entry : Constants.notificationsDrawables.entrySet()) {
            Drawable drawable = entry.getValue();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            ImageView icon = new ImageView(getApplicationContext());
            icon.setImageDrawable(drawable);
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
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
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

    private boolean isNotificationServiceRunning() {
        Class<?> serviceClass = NotificationListener.class;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(NOTIFICATION_LOG_TAG, " Is already running");
                return true;
            }
        }
        Log.d(NOTIFICATION_LOG_TAG, " Is not running");
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        enableButtonBacklight();
        Constants.isShown = false;
        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(frameLayout);
            WakeLock1.release();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
        }
        setBrightness(originalBrightness, originalAutoBrightnessStatus);
        unregisterReceiver(unlockReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                            Log.d(LOG_TAG, "Swipe right");
                        } else {
                            Log.d(LOG_TAG, "Swipe left");
                        }
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        Log.d(LOG_TAG, "Swipe bottom");
                    } else {
                        Log.d(LOG_TAG, "Swipe top");
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
                Log.d(LOG_TAG, "Double tap");
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