package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.Services.MainService;

import static android.content.Context.POWER_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {

    private static final String TAG = ScreenReceiver.class.getSimpleName();
    private static final String WAKE_LOCK_TAG = "ScreenOnWakeLock";

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

    Prefs prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = new Prefs(context);
        prefs.apply();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Globals.sensorIsScreenOff = true;
            Log.i(TAG, "Screen turned off\nShown:" + Globals.isShown);
            if (Globals.isShown) {
                // Screen turned off with service running, wake up device
                turnScreenOn(context, true);
            } else {
                // Start service when screen is off
                if (!Globals.inCall && prefs.getByKey("enabled",true))
                    context.startService(new Intent(context, MainService.class));
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG, "Screen turned on\nShown:" + Globals.isShown);
        }
    }
}
