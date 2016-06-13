package com.tomer.alwayson.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.ScreenReceiver;

public class StarterService extends Service {
    private BroadcastReceiver mReceiver;
    private boolean isRegistered = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Starter Service started");

        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();

        if (prefs.enabled) {
            if (prefs.showNotification) {
                showNotification();
            }
            if (prefs.notificationsAlerts) {
                startService(new Intent(getApplicationContext(), NotificationListener.class));
            }
            registerReceiver();
        } else {
            hideNotification();
            unregisterReceiver();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideNotification();
        unregisterReceiver();
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.notification_message));
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setSmallIcon(android.R.color.transparent);
        Notification notification = builder.build();
        NotificationManager notificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManger.notify(1, notification);
    }

    private void hideNotification() {
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    private void registerReceiver() {
        unregisterReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        isRegistered = true;
    }

    private void unregisterReceiver() {
        if (!isRegistered) {
            return;
        }
        try {
            unregisterReceiver(mReceiver);
            if (mReceiver.isOrderedBroadcast())
                mReceiver.abortBroadcast();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRegistered = false;
        }
    }
}
