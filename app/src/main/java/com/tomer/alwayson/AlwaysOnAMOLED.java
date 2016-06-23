package com.tomer.alwayson;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.oneskyapp.screenshot.OneSkyScreenshotHelper;

public class AlwaysOnAMOLED extends Application {
    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }
}
