package com.tomer.alwayson.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.ScreenReceiver;

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
        unregisterReceiver();
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
        else
            hideNotification();

        if (prefs.enabled) {
            unregisterReceiver();
            registerReceiver(mReceiver, filter);
            if (prefs.notificationsAlerts) {
                startService(new Intent(getApplicationContext(), NotificationListener.class));
            }
        } else {
            hideNotification();
            unregisterReceiver();
        }

    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.notification_message));
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MIN);
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

    void unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        try{
            mReceiver.abortBroadcast();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
