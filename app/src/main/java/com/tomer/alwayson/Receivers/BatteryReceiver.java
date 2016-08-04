package com.tomer.alwayson.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;

public class BatteryReceiver extends BroadcastReceiver implements ContextConstatns {
    TextView batteryTV;
    ImageView batteryIV;
    public int currentBattery;
    public BatteryReceiver(TextView textView, ImageView imageView){
        this.batteryIV = imageView;
        this.batteryTV = textView;
    }

    @Override
    public void onReceive(Context ctxt, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean charging = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        Log.d(MAIN_SERVICE_LOG_TAG, "Battery level " + level);
        Log.d(MAIN_SERVICE_LOG_TAG, "Battery charging " + charging);
        batteryTV.setText(String.valueOf(level) + "%");
        currentBattery = level;
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

