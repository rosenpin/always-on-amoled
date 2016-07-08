package com.tomer.alwayson.Services;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;

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
        Log.d(NOTIFICATION_LISTENER_TAG, "New notification from" + added.getPackageName());
        if (added.isClearable()) {
            Globals.notificationsDrawables.put(getUniqueKey(added), getIcon(added));
            Globals.notificationChanged = true;
            Globals.newNotification = new Notification(added.getNotification().extras.getString("android.title"), added.getNotification().extras.getString("android.text"), getIcon(added));
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

    public static class Notification {
        Drawable icon;
        String title, message;
        Intent intent;

        public Notification(String title, String message, Drawable icon) {
            this.icon = icon;
            this.title = title;
            this.message = message;
            this.intent = intent;
        }

        public Drawable getIcon() {
            icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public Intent getIntent() {
            return intent;
        }

        interface NotificationHandler {
            public void onNotificationArrived();
        }

    }


}
