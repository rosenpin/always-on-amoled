package com.tomer.alwayson.Services;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

/**
 * Created by tomer on 6/9/16.
 */

public class ServiceTurnOn extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager.WakeLock WakeLock1 = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(268435482, "WAKEUP");
        WakeLock1.acquire();
    }

}
