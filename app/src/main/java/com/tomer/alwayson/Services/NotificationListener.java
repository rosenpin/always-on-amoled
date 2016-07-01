package com.tomer.alwayson.Services;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;

public class NotificationListener extends NotificationListenerService implements ContextConstatns {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(NOTIFICATION_LISTENER_TAG, "started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(NOTIFICATION_LISTENER_TAG, "destroyed");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification added) {
        Log.i(NOTIFICATION_LISTENER_TAG, "New notification from" + added.getPackageName());
        if (added.isClearable()) {
            Globals.notificationsDrawables.put(getUniqueKey(added), getIcon(added));
            Globals.notificationChanged = true;
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification removed) {
        Log.d(NOTIFICATION_LISTENER_TAG, "Notification removed " + removed.getNotification().tickerText);
        Globals.notificationsDrawables.remove(getUniqueKey(removed));
        Globals.notificationChanged = true;
    }

    private String getUniqueKey(StatusBarNotification notification) {
        return notification.getPackageName().concat(":").concat(String.valueOf(notification.getId()));
    }

    private Drawable getIcon(StatusBarNotification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return notification.getNotification().getSmallIcon().loadDrawable(this);
        } else {
            return getResources().getDrawable(notification.getNotification().icon);
        }
    }
}
