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
        context.stopService(intent1);
        Constants.isShown = false;
    }
}
