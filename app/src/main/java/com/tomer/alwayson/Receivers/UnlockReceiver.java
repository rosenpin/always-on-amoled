package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Services.MainService;

public class UnlockReceiver extends BroadcastReceiver implements ContextConstatns {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainService.class);
        Log.i(UNLOCK_RECEIVER_TAG, "Received");
        context.stopService(intent1);
        Globals.isShown = false;
    }
}
