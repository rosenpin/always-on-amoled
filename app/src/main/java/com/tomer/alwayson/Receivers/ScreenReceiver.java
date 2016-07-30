package com.tomer.alwayson.Receivers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.Services.MainService;

import static android.content.Context.POWER_SERVICE;

public class ScreenReceiver extends BroadcastReceiver implements ContextConstatns {

    private static final String TAG = ScreenReceiver.class.getSimpleName();
    private static final String WAKE_LOCK_TAG = "ScreenOnWakeLock";
    Prefs prefs;
    private Context context;

    public static void turnScreenOn(Context c, boolean stopService) {
        try {
            if (stopService) {
                c.stopService(new Intent(c, MainService.class));
                Globals.isShown = false;
            }
            @SuppressWarnings("deprecation")
            PowerManager.WakeLock wl = ((PowerManager) c.getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKE_LOCK_TAG);
            wl.acquire();
            wl.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean doesDeviceHaveSecuritySetup(Context context) {
        return isPatternSet(context) || isPassOrPinSet(context);
    }

    private static boolean isPatternSet(Context context) {
        ContentResolver cr = context.getContentResolver();
        try {
            int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
            return lockPatternEnable == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    private static boolean isPassOrPinSet(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        prefs = new Prefs(context);
        prefs.apply();

        this.context = context;

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Globals.sensorIsScreenOff = true;
            Log.i(TAG, "Screen turned off\nShown:" + Globals.isShown);
            if (Globals.isShown) {
                // Screen turned off with service running, wake up device
                turnScreenOn(context, true);
            } else {
                //Checking if was killed by delay or naturally, if so, don't restart the service
                if (Globals.killedByDelay) {
                    Globals.killedByDelay = false;
                    Log.d(SCREEN_RECEIVER_LOG_TAG, "Killed by delay and won't restart");
                    return;
                }
                // Start service when screen is off
                if (!Globals.inCall && prefs.enabled) {
                    boolean toStart = shouldStart();
                    Log.d("SHOULD START ", String.valueOf(toStart));
                    if (toStart) {
                        if (prefs.startAfterLock) {
                            final KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                            if (myKM.inKeyguardRestrictedInputMode()) {
                                //Screen is locked, start the service
                                Log.d(SCREEN_RECEIVER_LOG_TAG, "Device is locked");
                                context.startService(new Intent(context, MainService.class));
                                Globals.isShown = true;
                            } else {
                                //Screen is unlocked, wait until the lock timeout is over before starting the service.
                                int startDelay;
                                if (!doesDeviceHaveSecuritySetup(context)) {
                                    Globals.noLock = true;
                                    context.startService(new Intent(context, MainService.class));
                                    Globals.isShown = true;
                                    Log.d(SCREEN_RECEIVER_LOG_TAG, "Device is unlocked");
                                } else {
                                    Log.d(SCREEN_RECEIVER_LOG_TAG, "Device is locked but has a timeout");
                                    try {
                                        startDelay = Settings.Secure.getInt(context.getContentResolver(), "lock_screen_lock_after_timeout", 5000);
                                        if (startDelay == -1)
                                            startDelay = (int) Settings.Secure.getLong(context.getContentResolver(), "lock_screen_lock_after_timeout", 5000);
                                        Log.d(SCREEN_RECEIVER_LOG_TAG, "Lock time out " + String.valueOf(startDelay));
                                    } catch (Exception settingNotFound) {
                                        startDelay = 0;
                                    }
                                    if (startDelay > 0) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (myKM.inKeyguardRestrictedInputMode()) {
                                                    context.startService(new Intent(context, MainService.class));
                                                    Globals.isShown = true;
                                                }
                                            }
                                        }, startDelay);
                                    } else {
                                        context.startService(new Intent(context, MainService.class));
                                        Globals.isShown = true;
                                    }
                                }
                            }
                        } else {
                            context.startService(new Intent(context, MainService.class));
                            Globals.isShown = true;
                        }
                    }
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG, "Screen turned on\nShown:" + Globals.isShown);
        }
    }

    private boolean isConnected() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert intent != null;
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    private boolean shouldStart() {
        prefs.apply();
        if (prefs.rules.equals("charging")) {
            return isConnected() && getBatteryLevel() > prefs.batteryRules;
        } else if (prefs.rules.equals("discharging")) {
            return !isConnected() && getBatteryLevel() > prefs.batteryRules;
        }
        return getBatteryLevel() > prefs.batteryRules;
    }

    public float getBatteryLevel() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }
}
