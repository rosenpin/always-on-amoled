package com.tomer.alwayson.helpers;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.tomer.alwayson.ContextConstatns;

public class GreenifyStarter implements ContextConstatns {

    private Context context;

    public GreenifyStarter(Context context) {
        this.context = context;
    }

    public void start(boolean greenifyEnabled) {
        if (greenifyEnabled && Utils.isPackageInstalled(context, "com.oasisfeng.greenify")) {
            Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Starting Greenify");
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.oasisfeng.greenify", "com.oasisfeng.greenify.GreenifyShortcut"));
            i.putExtra("noop-toast", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(i);
                Utils.logDebug(GREENIFY_STARTER, "Started");
            } catch (ActivityNotFoundException ignored) {
                Utils.logDebug(GREENIFY_STARTER, "Failed to start");
            }
        }
    }
}
