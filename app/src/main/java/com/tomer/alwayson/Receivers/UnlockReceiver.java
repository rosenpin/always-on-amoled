package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.tomer.alwayson.Constants;
import com.tomer.alwayson.Services.MainService;

/**
 * Created by tomer on 6/13/16.
 */
public class UnlockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MainService.class);
        System.out.println("I was summoned here!");
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)||intent.getAction().equals(Intent.ACTION_ALL_APPS)) {
            context.stopService(intent1);
            Constants.isShown = false;
        }
    }
}
