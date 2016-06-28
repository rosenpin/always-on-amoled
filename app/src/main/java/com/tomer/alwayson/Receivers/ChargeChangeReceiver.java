package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.Services.MainService;

public class ChargeChangeReceiver extends BroadcastReceiver implements ContextConstatns {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        prefs.apply();
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Log.d(CHARGER_RECEIVER_LOG_TAG, "Connected");
            if (prefs.rules.equals("discharging")) {
                if (Globals.isShown) {
                    context.stopService(new Intent(context, MainService.class));
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Log.d(CHARGER_RECEIVER_LOG_TAG, "Disconnected");
            if (prefs.rules.equals("charging")) {
                if (Globals.isShown) {
                    context.stopService(new Intent(context, MainService.class));
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            Log.d(CHARGER_RECEIVER_LOG_TAG, "Battery changed");
            if (prefs.batteryRules < getBatteryLevel(intent)) {
                if (Globals.isShown) {
                    context.stopService(new Intent(context, MainService.class));
                }
            }
        }
    }

    public float getBatteryLevel(Intent batteryIntent) {
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }
}
