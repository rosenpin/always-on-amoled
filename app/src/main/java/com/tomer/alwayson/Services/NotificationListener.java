package com.tomer.alwayson.Services;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.tomer.alwayson.Constants;

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification added) {
        if (!added.getPackageName().equals("com.tomer.alwayson")) {
            Constants.notificationsDrawables.put(getUniqueKey(added), getIcon(added));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification removed) {
        Constants.notificationsDrawables.remove(getUniqueKey(removed));
    }

    private String getUniqueKey(StatusBarNotification notification) {
        return notification.getPackageName().concat(":").concat(String.valueOf(notification.getId()));
    }

    private Drawable getIcon(StatusBarNotification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return notification.getNotification().getSmallIcon().loadDrawable(this);
        } else {
            //noinspection deprecation
            return getResources().getDrawable(notification.getNotification().icon);
        }
    }
}
