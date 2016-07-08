package com.tomer.alwayson.Services;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
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
        Log.d(NOTIFICATION_LISTENER_TAG, "New notification from" + added.getPackageName());
        if (added.isClearable() && added.getNotification().priority >= android.app.Notification.PRIORITY_LOW) {
            Globals.notificationsDrawables.put(getUniqueKey(added), getIcon(added));
            Globals.notificationChanged = true;
            String title = "" + added.getNotification().extras.getString("android.title");
            String content = "" + added.getNotification().extras.getString("android.text");
            Drawable icon = getIcon(added);
            Globals.newNotification = new Notification(this, title, content, icon);
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
        private Drawable icon;
        private String title, message;
        private Context context;

        public Notification(Context context, String title, String message, Drawable icon) {
            this.icon = icon;
            this.title = title;
            this.message = message;
            this.context = context;
        }

        public Drawable getIcon() {
            icon.mutate().setColorFilter(ContextCompat.getColor(context, android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY);
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }
    }


}
