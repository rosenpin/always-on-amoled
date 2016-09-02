package com.tomer.alwayson.helpers;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;

import java.io.IOException;

public class BatterySaver implements ContextConstatns {
    public boolean originalBatterySaverMode;
    private Context context;

    public BatterySaver(Context context) {
        this.context = context;
        this.originalBatterySaverMode = Settings.Global.getInt(context.getContentResolver(), LOW_POWER, 0) == 1;
    }

    public void setSystemBatterySaver(boolean status) {
        try {
            Settings.Global.putInt(context.getContentResolver(), LOW_POWER, status ? 1 : 0);
        } catch (SecurityException ignored) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global low_power " + (status ? 1 : 0)});
                process.waitFor();
            } catch (InterruptedException | IOException e) {
                Log.i(MAIN_SERVICE_LOG_TAG, "User doesn't have root");
            }
        }
    }
}
