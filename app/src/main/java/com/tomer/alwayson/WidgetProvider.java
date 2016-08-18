package com.tomer.alwayson;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.services.ToggleService;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Prefs prefs = new Prefs(context);
        prefs.apply();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Intent configIntent = new Intent(context, ToggleService.class);

        remoteViews.setTextColor(R.id.toggle, ContextCompat.getColor(context, !prefs.enabled ? android.R.color.holo_red_light : android.R.color.holo_green_light));
        remoteViews.setTextViewText(R.id.toggle, !prefs.enabled ? context.getString(R.string.widget_off) : context.getString(R.string.widget_on));

        PendingIntent configPendingIntent = PendingIntent.getService(context, 0, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.toggle, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
