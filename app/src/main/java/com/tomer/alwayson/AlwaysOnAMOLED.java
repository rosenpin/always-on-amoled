package com.tomer.alwayson;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.tomer.alwayson.helpers.Utils;

public class AlwaysOnAMOLED extends Application {
    static {
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        } catch (NoClassDefFoundError e) {
            Utils.logInfo("Error in application", "Android failed to do its job.");
        }
    }
}
