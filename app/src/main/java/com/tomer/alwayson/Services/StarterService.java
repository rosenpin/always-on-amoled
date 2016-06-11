package com.tomer.alwayson.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.Receivers.ScreenReceiver;

/**
 * Created by tomer on 6/11/16.
 */
public class StarterService extends Service {
    Prefs prefs;
    BroadcastReceiver mReceiver;
    IntentFilter filter;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideNotification();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Starter Service started");

        prefs = new Prefs(getApplicationContext());
        prefs.apply();

        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenReceiver();

        if (prefs.showNotification)
            showNotification();

        if (prefs.enabled) {
            unregisterReceiver();
            registerReceiver(mReceiver, filter);
            startService(new Intent(getApplicationContext(),NotificationListener.class));
        }

    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Always On Is Running");
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_LOW);
        builder.setSmallIcon(android.R.color.transparent);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }

    private void hideNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
    }

    boolean unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
