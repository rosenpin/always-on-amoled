package com.tomer.alwayson.helpers;

import android.content.Context;
import android.provider.Settings;

import com.tomer.alwayson.ContextConstatns;

import java.io.IOException;

public class BatterySaver implements ContextConstatns {
    private Context context;

    public BatterySaver(Context context) {
        this.context = context;
    }

    public void setSystemBatterySaver(boolean status) {
        try {
            Settings.Global.putInt(context.getContentResolver(), LOW_POWER, status ? 1 : 0);
        } catch (SecurityException e) {
            e.printStackTrace();
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global low_power " + (status ? 1 : 0)});
                process.waitFor();
            } catch (InterruptedException | IOException ignored) {
            }
        }
    }
}
