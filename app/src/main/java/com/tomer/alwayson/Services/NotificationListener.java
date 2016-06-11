package com.tomer.alwayson.Services;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.tomer.alwayson.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tomer on 6/11/16.
 */
public class NotificationListener extends NotificationListenerService {
    Context context;

    ArrayList<String> packagesName = new ArrayList<>();

    @Override
    public void onNotificationPosted(StatusBarNotification newsbn) {
        try {
            if (!Constants.notificationsDrawables.contains(getDrawableByPN(newsbn.getPackageName())) && !packagesName.contains(newsbn.getPackageName()) && !newsbn.getPackageName().equals("com.tomer.alwayson")) {
                Constants.notificationsDrawables.add(getDrawableByPN(newsbn.getPackageName()));
                packagesName.add(newsbn.getPackageName());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification newsbn) {
        getActiveNotifications();
    }

    Drawable getDrawableByPN(String packageName) throws PackageManager.NameNotFoundException {
        return getPackageManager().getApplicationIcon(packageName);
    }
}
