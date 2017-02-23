package com.tomer.alwayson.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;

public class DAReceiver extends DeviceAdminReceiver {

    Prefs prefs;

    void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.warning_5_device_admin_disable);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.warning_6_device_admin_disabled));
        prefs = new Prefs(context);
        prefs.apply();
        prefs.setBool(Prefs.KEYS.PROXIMITY_TO_LOCK.toString(), false);
    }

}


