package com.tomer.alwayson.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tomer.alwayson.services.MainService;

public final class FireReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        context.startService(new Intent(context, MainService.class));
    }
}
