package com.tomer.alwayson;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.tomer.alwayson.activities.ReporterActivity;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.StarterService;

public class AlwaysOnAMOLED extends Application {
    public static int reportNotificationID = 53;

    static {
        try {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_AUTO);
        } catch (NoClassDefFoundError e) {
            Utils.logInfo("Error in application", "Android failed to do its job.");
        }
    }
}
