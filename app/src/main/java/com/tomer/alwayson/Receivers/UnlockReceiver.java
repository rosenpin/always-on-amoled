package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.Services.MainService;

public class UnlockReceiver extends BroadcastReceiver {
    private static String TAG = UnlockReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainService.class);
        Log.i(TAG, "Received");
        //if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) || intent.getAction().equals(Intent.ACTION_ALL_APPS) || intent.getAction().equals(Intent.ACTION_ALL_APPS) || intent.getAction().equals("com.android.deskclock.ALARM_ALERT")) {
        context.stopService(intent1);
        Constants.isShown = false;
        //}
    }
}
