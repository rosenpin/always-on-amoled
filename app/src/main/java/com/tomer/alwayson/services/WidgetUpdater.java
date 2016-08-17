package com.tomer.alwayson.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.WidgetProvider;

public class WidgetUpdater extends Service implements ContextConstatns {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(WIDGET_UPDATER_TAG, "Started");
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();

        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        if (!prefs.enabled) {
            remoteViews.setTextColor(R.id.toggle, context.getResources().getColor(android.R.color.holo_red_light));
            remoteViews.setTextViewText(R.id.toggle, context.getString(R.string.widget_off));
        } else {
            remoteViews.setTextColor(R.id.toggle, context.getResources().getColor(android.R.color.holo_green_light));
            remoteViews.setTextViewText(R.id.toggle, context.getString(R.string.widget_on));
        }
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(WIDGET_UPDATER_TAG, "Destroyed");
    }
}
