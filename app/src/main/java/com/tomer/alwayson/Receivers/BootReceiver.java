package com.tomer.alwayson.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.Services.StarterService;

/**
 * Created by tomer on 6/9/16.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        if (prefs.enabled) {
            context.startService(new Intent(context, StarterService.class));
        }
        if (prefs.showNotification)
            showNotification(context);
    }

    private void showNotification(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Always On Is Running");
        builder.setOngoing(true);
        builder.setSmallIcon(android.R.color.transparent);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }

    private void hideNotification(Context context) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancelAll();
    }
}
