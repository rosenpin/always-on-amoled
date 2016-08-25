package com.tomer.alwayson.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;

public class BatteryReceiver extends BroadcastReceiver implements ContextConstatns {
    public int currentBattery;
    TextView batteryTV;
    ImageView batteryIV;
    boolean updateText;

    public BatteryReceiver(TextView textView, ImageView imageView) {
        this.batteryIV = imageView;
        this.batteryTV = textView;
        this.updateText = true;
    }

    public BatteryReceiver() {
        this.updateText = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean charging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        currentBattery = level;
        Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Battery level " + level);

        if (batteryTV != null)
            batteryTV.setText(String.valueOf(level) + "%");
        if (batteryIV != null) {
            int res;
            if (charging)
                res = R.drawable.ic_battery_charging;
            else {
                if (level > 90)
                    res = R.drawable.ic_battery_full;
                else if (level > 70)
                    res = R.drawable.ic_battery_90;
                else if (level > 50)
                    res = R.drawable.ic_battery_60;
                else if (level > 30)
                    res = R.drawable.ic_battery_30;
                else if (level > 20)
                    res = R.drawable.ic_battery_20;
                else if (level > 0)
                    res = R.drawable.ic_battery_alert;
                else
                    res = R.drawable.ic_battery_unknown;
            }
            batteryIV.setImageResource(res);
        }
    }

    public int getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            return (level / scale) * 100;
        }
        return -1;
    }
}

