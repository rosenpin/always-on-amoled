package com.tomer.alwayson.services;

import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.tomer.alwayson.R;
import com.tomer.alwayson.WidgetProvider;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;

public class ToggleService extends Service {
    private Intent starterServiceIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.logInfo(ToggleService.class.getSimpleName(), "Started toggle service");
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();
        starterServiceIntent = new Intent(getApplicationContext(), StarterService.class);
        prefs.setBool(Prefs.KEYS.ENABLED.toString(), !prefs.enabled);

        hideNotification();
        restartService();

        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

        remoteViews.setTextColor(R.id.toggle, ContextCompat.getColor(context, prefs.enabled ? android.R.color.holo_red_light : android.R.color.holo_green_light));
        remoteViews.setTextViewText(R.id.toggle, prefs.enabled ? context.getString(R.string.widget_off) : context.getString(R.string.widget_on));

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        stopSelf();
    }

    private void restartService() {
        stopService(starterServiceIntent);
        startService(starterServiceIntent);
    }

    private void hideNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
    }
}
