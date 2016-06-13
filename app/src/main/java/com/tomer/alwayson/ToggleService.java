package com.tomer.alwayson;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.Services.StarterService;

/**
 * Created by tomer AKA rosenpin on 6/13/16.
 */
public class ToggleService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private Intent starterServiceIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Started service");
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.apply();
        starterServiceIntent = new Intent(getApplicationContext(), StarterService.class);
        prefs.setBool(Prefs.KEYS.ENABLED.toString(), !prefs.enabled);
        ScreenReceiver screenReceiver = new ScreenReceiver();
        try{unregisterReceiver(screenReceiver);}catch (Exception ignore){}
        restartService();

        stopSelf();
    }

    private void restartService() {
        stopService(starterServiceIntent);
        startService(starterServiceIntent);
    }

}
