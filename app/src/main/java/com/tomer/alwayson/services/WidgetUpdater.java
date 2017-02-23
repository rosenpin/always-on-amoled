package com.tomer.alwayson.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;
import com.tomer.alwayson.WidgetProvider;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;

public class WidgetUpdater extends Service implements ContextConstatns {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.logInfo(WIDGET_UPDATER_TAG, "Started");
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
        ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
        if (!prefs.enabled) {
            remoteViews.setTextColor(R.id.toggle, getResources().getColor(android.R.color.holo_red_light));
            remoteViews.setTextViewText(R.id.toggle, getString(R.string.widget_off));
        } else {
            remoteViews.setTextColor(R.id.toggle, getResources().getColor(android.R.color.holo_green_light));
            remoteViews.setTextViewText(R.id.toggle, getString(R.string.widget_on));
        }
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.logInfo(WIDGET_UPDATER_TAG, "Destroyed");
    }
}
