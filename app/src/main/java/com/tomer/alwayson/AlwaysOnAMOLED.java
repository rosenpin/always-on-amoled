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

import java.util.Arrays;

public class AlwaysOnAMOLED extends Application {
    static {
        try {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_AUTO);
        } catch (NoClassDefFoundError e) {
            Log.i("Error in application", "Android failed to do its job.");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(e);
            }
        });
    }

    public static int reportNotificationID = 53;

    public void handleUncaughtException(Throwable e) {
        int reportNotificationID = 53;
        Context context = getApplicationContext();
        Log.d("Exception now!", "exeption");
        e.printStackTrace();
        Toast.makeText(context, R.string.error_0_unknown_error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, ReporterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++)
        {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

// If the exception was thrown in a background thread inside
// AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++)
            {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";


        intent.putExtra("log", report);
        PendingIntent reportIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Utils.showErrorNotification(context, context.getString(R.string.error), context.getString(R.string.error_0_unknown_error_report_prompt), reportNotificationID, reportIntent);
        System.exit(0);
        startService(new Intent(getApplicationContext(), StarterService.class));
    }
}
