package com.tomer.alwayson.Helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;

public class GreenifyStarter implements ContextConstatns {

    private Context context;

    public GreenifyStarter(Context context) {
        this.context = context;
    }

    public void start(boolean greenifyEnabled) {
        if (greenifyEnabled && Utils.isPackageInstalled(context, "com.oasisfeng.greenify")) {
            Log.d(MAIN_SERVICE_LOG_TAG, "Starting Greenify");
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.oasisfeng.greenify", "com.oasisfeng.greenify.GreenifyShortcut"));
            i.putExtra("noop-toast", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            Log.d(GREENIFY_STARTER,"Started");
        }
    }
}
