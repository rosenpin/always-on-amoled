package com.tomer.alwayson.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.StarterService;

public class BootReceiver extends BroadcastReceiver implements ContextConstatns {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        prefs.apply();
        if (prefs.enabled)
            context.startService(new Intent(context, StarterService.class));
        Utils.logInfo(BOOT_RECEIVER, "started");
    }
}
