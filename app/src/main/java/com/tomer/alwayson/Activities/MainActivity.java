package com.tomer.alwayson.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.ScreenReceiver;


public class MainActivity extends AppCompatActivity {
    Prefs prefs;
    BroadcastReceiver mReceiver;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();

        prefs = new Prefs(getApplicationContext());
        prefs.apply();

        if (prefs.showNotification)
            showNotification();

        if (prefs.enabled) {
            unregisterReceiver();
            registerReceiver(mReceiver, filter);
        }

        handleBoolSimplePref((Switch) findViewById(R.id.cb_touch_to_stop), Prefs.KEYS.TOUCH_TO_STOP.toString(), prefs.touchToStop);
        handleBoolSimplePref((Switch) findViewById(R.id.cb_swipe_to_stop), Prefs.KEYS.SWIPE_TO_STOP.toString(), prefs.swipeToStop);
        handleBoolSimplePref((Switch) findViewById(R.id.cb_show_notification), Prefs.KEYS.SHOW_NOTIFICATION.toString(), prefs.showNotification);
        handleBoolSimplePref((Switch) findViewById(R.id.cb_enabled), Prefs.KEYS.ENABLED.toString(), prefs.enabled);

        handlePermissions();
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Always On Is Running");
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_LOW);
        builder.setSmallIcon(android.R.color.transparent);
        Notification notification = builder.build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }

    private void hideNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
    }

    private void handlePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.READ_PHONE_STATE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this,
                        permission) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{permission},
                            123);
                }
            }
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        try {
            View view = new View(getApplicationContext());
            ((WindowManager) getSystemService("window")).addView(view, lp);
            ((WindowManager) getSystemService("window")).removeView(view);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    boolean unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    void handleBoolSimplePref(Switch cb, final String prefName, boolean val) {
        cb.setChecked(val);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setBool(prefName, isChecked);

                if (prefName.equals(Prefs.KEYS.TOUCH_TO_STOP.toString())) {
                    if (isChecked) {
                        ((Switch) findViewById(R.id.cb_swipe_to_stop)).setEnabled(false);
                        ((Switch) findViewById(R.id.cb_swipe_to_stop)).setChecked(false);
                    } else
                        ((Switch) findViewById(R.id.cb_swipe_to_stop)).setEnabled(true);
                } else if (prefName.equals(Prefs.KEYS.SWIPE_TO_STOP.toString())) {
                    if (isChecked) {
                        ((Switch) findViewById(R.id.cb_touch_to_stop)).setEnabled(false);
                        ((Switch) findViewById(R.id.cb_touch_to_stop)).setChecked(false);
                    } else
                        ((Switch) findViewById(R.id.cb_touch_to_stop)).setEnabled(true);
                } else if (prefName.equals(Prefs.KEYS.SHOW_NOTIFICATION.toString())) {
                    if (isChecked)
                        showNotification();
                    else
                        hideNotification();
                } else if (prefName.equals(Prefs.KEYS.ENABLED.toString())) {
                    if (isChecked) {
                        unregisterReceiver();
                        registerReceiver(mReceiver, filter);
                    } else {
                        unregisterReceiver();
                    }
                }
            }
        });
    }


}
