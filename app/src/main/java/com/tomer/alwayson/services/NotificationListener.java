package com.tomer.alwayson.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.Utils;

public class NotificationListener extends NotificationListenerService implements ContextConstatns {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.logDebug(NOTIFICATION_LISTENER_TAG, "started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logDebug(NOTIFICATION_LISTENER_TAG, "destroyed");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification added) {
        if (added.isClearable() && added.getNotification().priority >= android.app.Notification.PRIORITY_LOW) {
            Globals.notificationChanged = true;
            String title = added.getNotification().extras.getString(Notification.EXTRA_TITLE)+ " ";
            if (title.equals("null"))
                title = added.getNotification().extras.getString(Notification.EXTRA_TITLE_BIG)+ " ";
            String content = added.getNotification().extras.getString(Notification.EXTRA_TEXT) + " ";
            if (content.equals("null") || content.isEmpty())
                content = added.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT_LINES)+ " ";
            if (content.equals("null") || content.isEmpty())
                content = added.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)+ " ";
            Drawable icon = getIcon(added);
            ApplicationInfo notificationAppInfo = null;
            try {
                notificationAppInfo = getPackageManager().getApplicationInfo(added.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Globals.newNotification = new NotificationHolder(this, title, content, icon, notificationAppInfo != null ? getPackageManager().getApplicationLabel(notificationAppInfo) : null, added.getNotification().contentIntent);
            Globals.notifications.put(getUniqueKey(added), Globals.newNotification);
        }
        if (Globals.onNotificationAction != null)
            Globals.onNotificationAction.run();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification removed) {
        Globals.notifications.remove(getUniqueKey(removed));
        Globals.notificationChanged = true;
        if (Globals.onNotificationAction != null)
            Globals.onNotificationAction.run();
    }

    private String getUniqueKey(StatusBarNotification notification) {
        return notification.getPackageName().concat(":").concat(String.valueOf(notification.getId()));
    }

    private Drawable getIcon(StatusBarNotification notification) {
        if (Utils.isAndroidNewerThanM()) {
            return notification.getNotification().getSmallIcon().loadDrawable(this);
        } else {
            return getResources().getDrawable(notification.getNotification().icon);
        }
    }

    public static class NotificationHolder {
        private String appName;
        private Drawable icon;
        private String title, message;
        private Context context;
        private PendingIntent intent;

        public NotificationHolder(Context context, String title, String message, Drawable icon, CharSequence appName, PendingIntent intent) {
            this.icon = icon;
            this.title = title;
            this.message = message;
            this.context = context;
            this.appName = (String) appName;
            if (this.message.equals("null"))
                this.message = "";
            if (this.title.equals("null"))
                this.title = "";
            this.intent = intent;
        }

        public Drawable getIcon() {
            if (icon != null)
                icon.mutate().setColorFilter(ContextCompat.getColor(context, android.R.color.primary_text_dark), PorterDuff.Mode.MULTIPLY);
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getAppName() {
            return appName;
        }

        public PendingIntent getIntent() {
            return intent;
        }
    }
}
