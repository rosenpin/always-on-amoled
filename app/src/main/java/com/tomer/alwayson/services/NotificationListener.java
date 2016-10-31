package com.tomer.alwayson.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import java.util.Map;

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
            String title = added.getNotification().extras.getString(Notification.EXTRA_TITLE) + " ";
            if (title.equals("null"))
                title = added.getNotification().extras.getString(Notification.EXTRA_TITLE_BIG) + " ";
            String content = added.getNotification().extras.getString(Notification.EXTRA_TEXT) + " ";
            if (content.equals("null") || content.isEmpty())
                content = added.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT_LINES) + " ";
            if (content.equals("null") || content.isEmpty())
                content = added.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) + " ";
            Drawable icon = getIcon(added);
            ApplicationInfo notificationAppInfo = null;
            try {
                notificationAppInfo = getPackageManager().getApplicationInfo(added.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(Globals.newNotification()!=null){
                Globals.notifications.get(Globals.newNotification()).setNew(false);
            }
            NotificationHolder newNotification = new NotificationHolder(title, content, icon, notificationAppInfo != null ? getPackageManager().getApplicationLabel(notificationAppInfo) : null, added.getNotification().contentIntent, true);
            Globals.notifications.put(getUniqueKey(added), newNotification);
        }
        sendBroadcast(new Intent(NEW_NOTIFICATION));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification removed) {
        Globals.notifications.remove(getUniqueKey(removed));
        sendBroadcast(new Intent(NEW_NOTIFICATION));
    }

    private String getUniqueKey(StatusBarNotification notification) {
        return notification.getPackageName();
    }


    private Drawable getIcon(StatusBarNotification notification) {
        if (Utils.isAndroidNewerThanM())
            return notification.getNotification().getSmallIcon().loadDrawable(this);
        else
            return ContextCompat.getDrawable(getApplicationContext(), notification.getNotification().icon);
    }



    public static class NotificationHolder {
        private String appName;
        private Drawable icon;
        private String title, message;
        private PendingIntent intent;
        private boolean isNew;


        NotificationHolder(String title, String message, Drawable icon, CharSequence appName, PendingIntent intent, boolean newnotification) {
            this.icon = icon;
            this.title = title;
            this.message = message;
            this.appName = (String) appName;
            if (this.message.equals("null"))
                this.message = "";
            if (this.title.equals("null"))
                this.title = "";
            this.intent = intent;
            this.isNew = newnotification;
        }


        public Drawable getIcon(Context context) {
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

        public boolean getNew(){return isNew;}

        public void setNew(boolean x){isNew = x;}

    }
}
