package com.tomer.alwayson;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by tomer AKA rosenpin on 6/18/16.
 */
public class AlwaysOnAMOLED extends Application {
    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }
}
