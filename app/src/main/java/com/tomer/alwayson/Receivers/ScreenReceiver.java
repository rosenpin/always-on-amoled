package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.Services.MainService;

import static android.content.Context.POWER_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {
    private static String TAG = ScreenReceiver.class.getSimpleName();
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received");
        Intent intent1 = new Intent(context, MainService.class);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG,"Screen turned off");
            if (!Constants.isShown) {
                context.startService(intent1);
                Constants.isShown = true;
            } else {
                try {
                    PowerManager.WakeLock wl = ((PowerManager) context.getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                    wl.acquire();
                    context.stopService(intent1);
                    Constants.isShown = false;
                    wl.release();
                } catch (Exception e) {
                    Log.d("Error: ", e.getMessage());
                }
            }
            wasScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG,"Screen turned on");
            wasScreenOn = true;
        }
    }
}
