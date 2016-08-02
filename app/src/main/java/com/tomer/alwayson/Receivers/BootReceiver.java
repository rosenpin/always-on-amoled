package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Helpers.Prefs;
import com.tomer.alwayson.Services.StarterService;

public class BootReceiver extends BroadcastReceiver implements ContextConstatns {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        prefs.apply();
        if (prefs.enabled)
            context.startService(new Intent(context, StarterService.class));
        Log.i(BOOT_RECEIVER, "started");
    }
}
