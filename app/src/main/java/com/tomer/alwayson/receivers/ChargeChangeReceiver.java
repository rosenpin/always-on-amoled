package com.tomer.alwayson.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;

public class ChargeChangeReceiver extends BroadcastReceiver implements ContextConstatns {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        prefs.apply();
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Utils.logDebug(CHARGER_RECEIVER_LOG_TAG, "Connected");
            if (prefs.rules.equals("discharging")) {
                if (Globals.isShown)
                    Utils.stopMainService(context);
            }
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Utils.logDebug(CHARGER_RECEIVER_LOG_TAG, "Disconnected");
            if (prefs.rules.equals("charging")) {
                if (Globals.isShown)
                    Utils.stopMainService(context);
            }
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            Utils.logDebug("Battery changed", String.valueOf(getBatteryLevel(intent)));
            Utils.logDebug("Min Battery to start", String.valueOf(prefs.batteryRules));
            Utils.logDebug(CHARGER_RECEIVER_LOG_TAG, "Battery changed");
            if (getBatteryLevel(intent) < prefs.batteryRules) {
                if (Globals.isServiceRunning)
                    Utils.stopMainService(context);
            } else {
                if (!isDisplayOn(context) && !Globals.isServiceRunning) {
                    Utils.stopMainService(context);
                }
            }
        }
    }

    private boolean isDisplayOn(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    return true;
                }
            }
            return false;
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isScreenOn();
        }
    }

    private float getBatteryLevel(Intent batteryIntent) {
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }
}
