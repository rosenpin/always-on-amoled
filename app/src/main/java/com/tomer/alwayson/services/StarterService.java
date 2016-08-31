package com.tomer.alwayson.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.receivers.ScreenReceiver;

public class StarterService extends Service {
    Intent notificationsAlertIntent;
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
        Utils.logInfo(StarterService.class.getSimpleName(), "Starter Service started");

        notificationsAlertIntent = new Intent(getApplicationContext(), NotificationListener.class);

        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();

        if (Utils.isAndroidNewerThanN())
            startService(new Intent(getApplicationContext(), QuickSettingsToggle.class));

        if (!isServiceRunning(WidgetUpdater.class)) {
            startService(new Intent(getApplicationContext(), WidgetUpdater.class));
        }

        if (prefs.enabled) {
            if (prefs.showNotification) {
                showNotification();
            }
            if (prefs.notificationsAlerts) {
                startService(notificationsAlertIntent);
            }
            registerReceiver();
        } else {
            hideNotification();
            unregisterReceiver();
            stopNotificationService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logInfo(StarterService.class.getSimpleName(), "Starter Service destroyed");
        hideNotification();
        unregisterReceiver();
        stopNotificationService();
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.notification_message));
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setSmallIcon(R.drawable.ic_notification);
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

    private void stopNotificationService() {

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        String TAG = serviceClass.getSimpleName();
        String serviceTag = serviceClass.getSimpleName();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Utils.logDebug(TAG, "Is already running");
                return true;
            }
        }
        Utils.logDebug(serviceTag, "Is not running");
        return false;
    }
}
