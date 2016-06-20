package com.tomer.alwayson.Activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SecretConstants;
import com.tomer.alwayson.Services.StarterService;
import com.tomer.alwayson.Services.WidgetUpdater;
import com.tomer.alwayson.SettingsFragment;

public class PreferencesActivity extends AppCompatActivity {
    Prefs prefs;
    private Intent starterServiceIntent;
    private Intent widgetUpdaterService;
    private IInAppBillingService mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = new Prefs(getApplicationContext());
        prefs.apply();

        getFragmentManager().beginTransaction()
                .replace(R.id.preferences_holder, new SettingsFragment())
                .commit();

        if (!prefs.permissionGranting && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(new Intent(getApplicationContext(), Intro.class));
            finish();
        } else {
            handlePermissions();

            starterServiceIntent = new Intent(getApplicationContext(), StarterService.class);
            widgetUpdaterService = new Intent(getApplicationContext(), WidgetUpdater.class);

            Intent serviceIntent =
                    new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

            donateButtonSetup();

            stopService(starterServiceIntent);
            startService(starterServiceIntent);
        }
    }

    private void donateButtonSetup() {
        Button donateButton = (Button) findViewById(R.id.donate);
        assert donateButton != null;
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String IAPID = SecretConstants.getPropertyValue(getBaseContext(), "IAPID");
                    String IAPID2 = SecretConstants.getPropertyValue(getBaseContext(), "IAPID2");
                    String IAPID3 = SecretConstants.getPropertyValue(getBaseContext(), "IAPID3");
                    String IAPID4 = SecretConstants.getPropertyValue(getBaseContext(), "IAPID4");
                    String googleIAPCode = SecretConstants.getPropertyValue(getBaseContext(), "googleIAPCode");
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            IAPID, "inapp", googleIAPCode);
                    Log.d("IAPID ", IAPID);

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    if (pendingIntent == null) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.thanks), Snackbar.LENGTH_LONG).show();
                        buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                IAPID2, "inapp", googleIAPCode);
                        pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        if (pendingIntent == null) {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.thanks_great), Snackbar.LENGTH_LONG).show();
                            buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                    IAPID3, "inapp", googleIAPCode);
                            pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                            if (pendingIntent == null) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.thanks_huge), Snackbar.LENGTH_LONG).show();
                                buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                        IAPID4, "inapp", googleIAPCode);
                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                if (pendingIntent == null) {
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.thanks_crazy), Snackbar.LENGTH_LONG).show();
                                } else {
                                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                                            1001, new Intent(), 0, 0,
                                            0);
                                }
                            } else {
                                startIntentSenderForResult(pendingIntent.getIntentSender(),
                                        1001, new Intent(), 0, 0,
                                        0);
                            }
                        } else {
                            startIntentSenderForResult(pendingIntent.getIntentSender(),
                                    1001, new Intent(), 0, 0,
                                    0);
                        }
                    } else {
                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                1001, new Intent(), 0, 0,
                                0);
                    }
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handlePermissions() {
        boolean phonePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        123);
                phonePermission = false;
            }
        }
        if (phonePermission) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

            try {
                View view = new View(getApplicationContext());
                ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.feedback:
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:")); // only email apps should handle this
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"tomerosenfeld007@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), getString(R.string.err_5_no_email_client), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
