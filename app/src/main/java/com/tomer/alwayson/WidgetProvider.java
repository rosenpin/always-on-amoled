package com.tomer.alwayson;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.tomer.alwayson.Services.ToggleService;

/**
 * Created by tomer AKA rosenpin on 6/13/16.
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Prefs prefs = new Prefs(context);
        prefs.apply();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Intent configIntent = new Intent(context, ToggleService.class);

        if (!prefs.enabled) {
            remoteViews.setTextColor(R.id.toggle, context.getResources().getColor(android.R.color.holo_red_light));
            remoteViews.setTextViewText(R.id.toggle, context.getString(R.string.off));
        } else {
            remoteViews.setTextColor(R.id.toggle, context.getResources().getColor(android.R.color.holo_green_light));
            remoteViews.setTextViewText(R.id.toggle, context.getString(R.string.on));
        }


        PendingIntent configPendingIntent = PendingIntent.getService(context, 0, configIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.toggle, configPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }


}