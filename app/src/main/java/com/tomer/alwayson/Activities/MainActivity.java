package com.tomer.alwayson.Activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.vending.billing.IInAppBillingService;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.SecretConstants;

import org.json.JSONException;
import org.json.JSONObject;


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
        filter.addAction(Intent.ACTION_USER_PRESENT);
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
        handleBoolSimplePref((Switch) findViewById(R.id.cb_move), Prefs.KEYS.MOVE_WIDGET.toString(), prefs.moveWidget);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        findViewById(R.id.donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            SecretConstants.IAPID, "inapp", SecretConstants.GoogleIAPCode);

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    if (pendingIntent == null) {
                        Snackbar.make(findViewById(android.R.id.content), "Thank you for your support! :)", Snackbar.LENGTH_LONG).show();
                    } else {
                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };


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

    @Override
    protected void onResume() {
        super.onResume();
        handlePermissions();
    }

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(getApplicationContext())){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {

            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(android.R.id.content), "Thank you for your support! :)", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }
}
