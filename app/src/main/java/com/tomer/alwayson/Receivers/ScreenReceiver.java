package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.Services.MainService;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomer on 6/9/16.
 */


public class ScreenReceiver extends BroadcastReceiver {

    // thanks Jason
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainService.class);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            System.out.println("Screen turned off");
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
            // and do whatever you need to do here
            System.out.println("Screen turned on");
            wasScreenOn = true;
        }
    }

}
