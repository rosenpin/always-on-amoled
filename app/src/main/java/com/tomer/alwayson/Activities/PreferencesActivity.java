package com.tomer.alwayson.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.android.vending.billing.IInAppBillingService;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.Receivers.DAReceiver;
import com.tomer.alwayson.SecretConstants;
import com.tomer.alwayson.Services.MainService;
import com.tomer.alwayson.Services.StarterService;
import com.tomer.alwayson.Services.WidgetUpdater;
import com.tomer.alwayson.SettingsFragment;

import fr.nicolaspomepuy.discreetapprate.AppRate;

public class PreferencesActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, ContextConstatns {
    Prefs prefs;
    Intent billingServiceIntent;
    private Intent starterServiceIntent;
    private Intent widgetUpdaterService;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean demo;

    public static void promptToSupport(final Activity context, final IInAppBillingService mService, final View rootView, boolean supporterFeature) {
        new MaterialDialog.Builder(context)
                .title(R.string.action_support_the_development)
                .content(supporterFeature ? R.string.supporter_feature_only : R.string.support_how_much)
                .items(R.array.support_items)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                String googleIAPCode = SecretConstants.getPropertyValue(context, "googleIAPCode");
                                Bundle buyIntentBundle;
                                PendingIntent pendingIntent = null;
                                try {
                                    switch (which) {
                                        case 0:
                                            String IAPID = SecretConstants.getPropertyValue(context, "IAPID");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 1:
                                            String IAPID2 = SecretConstants.getPropertyValue(context, "IAPID2");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID2, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks_great), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 2:
                                            String IAPID3 = SecretConstants.getPropertyValue(context, "IAPID3");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID3, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks_huge), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 3:
                                            String IAPID4 = SecretConstants.getPropertyValue(context, "IAPID4");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID4, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks_crazy), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 4:
                                            String IAPID5 = SecretConstants.getPropertyValue(context, "IAPID5");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID5, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks_crazy), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                        case 5:
                                            String IAPID6 = SecretConstants.getPropertyValue(context, "IAPID6");
                                            try {
                                                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                                                        IAPID6, "inapp", googleIAPCode);
                                                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                                if (pendingIntent == null)
                                                    Snackbar.make(rootView, context.getString(R.string.thanks_crazy), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                    }
                                    context.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                                } catch (Exception e) {
                                    Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error), Snackbar.LENGTH_LONG).show();
                                }

                                return true;
                            }
                        }
                ).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(getApplicationContext());
        prefs.apply();
        resetPaymentService();
        if (!prefs.permissionGranting) {
            startActivity(new Intent(getApplicationContext(), Intro.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getFragmentManager().beginTransaction()
                    .replace(R.id.preferences_holder, new SettingsFragment())
                    .commit();

            handlePermissions();

            starterServiceIntent = new Intent(getApplicationContext(), StarterService.class);
            widgetUpdaterService = new Intent(getApplicationContext(), WidgetUpdater.class);

            billingServiceIntent =
                    new Intent("com.android.vending.billing.InAppBillingService.BIND");
            billingServiceIntent.setPackage("com.android.vending");
            bindService(billingServiceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

            donateButtonSetup();

            findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    demo = true;
                    Intent demoService = new Intent(getApplicationContext(), MainService.class);
                    demoService.putExtra("demo", true);
                    startService(demoService);
                }
            });

            appRate();

            stopService(starterServiceIntent);
            startService(starterServiceIntent);

            Globals.colorDialog = new ColorChooserDialog.Builder(this, R.string.settings_text_color)
                    .titleSub(R.string.settings_text_color_desc)
                    .doneButton(R.string.md_done_label)
                    .cancelButton(R.string.md_cancel_label)
                    .backButton(R.string.md_back_label)
                    .preselect(-1)
                    .accentMode(true)
                    .dynamicButtonColor(false);
        }
    }

    private void appRate() {
        AppRate.with(this).starRating(true).starRatingListener(new AppRate.OnStarRateListener() {
            @Override
            public void onPositiveRating(int starRating) {
                Toast.makeText(PreferencesActivity.this, R.string.thanks_short, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNegativeRating(int starRating) {
                startActivity(new Intent(getApplicationContext(), ReporterActivity.class));
                Toast.makeText(PreferencesActivity.this, R.string.warning_12_please_report, Toast.LENGTH_LONG).show();
            }
        }).checkAndShow();
    }

    private void donateButtonSetup() {
        Button donateButton = (Button) findViewById(R.id.donate);
        assert donateButton != null;
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesActivity.promptToSupport(PreferencesActivity.this, mService, findViewById(android.R.id.content), false);
            }
        });

    }

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA},
                        123);
            }
        }

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

    public static void uninstall(Context context, Prefs prefs) {
        try {
            ComponentName devAdminReceiver = new ComponentName(context, DAReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.removeActiveAdmin(devAdminReceiver);
            if (prefs.proximityToLock == 2)
                prefs.setString(Prefs.KEYS.PROXIMITY_TO_LOCK.toString(), "0");
        } catch (Exception ignored) {
        }
        Uri packageUri = Uri.parse("package:" + context.getPackageName());
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
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
                startActivity(new Intent(getApplicationContext(), ReporterActivity.class));
                return true;
            case R.id.uninstall:
                uninstall(this, prefs);
                return true;
            case R.id.community:
                SettingsFragment.openURL("https://plus.google.com/communities/104206728795122451273", this);
                return true;
            case R.id.github:
                SettingsFragment.openURL("https://github.com/rosenpin/AlwaysOnDisplayAmoled", this);
                return true;
            case R.id.translate:
                SettingsFragment.openURL("https://crowdin.com/project/always-on-amoled", this);
                return true;
            case R.id.rate:
                SettingsFragment.openPlayStoreUrl(getPackageName(), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mServiceConn);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        Log.d(ContextConstatns.MAIN_SERVICE_LOG_TAG, String.valueOf(selectedColor));
        prefs.setInt(Prefs.KEYS.TEXT_COLOR.toString(), selectedColor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Log.d("Purchase state", String.valueOf(resultCode));
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.thanks, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.thanks, 10000).setAction(R.string.action_restart, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                    }
                }).show();
                Log.d("User bought item", data.getStringExtra("INAPP_PURCHASE_DATA"));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isServiceRunning(MainService.class)) {
            stopService(new Intent(getApplicationContext(), MainService.class));
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isServiceRunning(MainService.class) && demo) {
            stopService(new Intent(getApplicationContext(), MainService.class));
            demo = false;
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        String TAG = serviceClass.getSimpleName();
        String serviceTag = serviceClass.getSimpleName();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Is already running");
                return true;
            }
        }
        Log.d(serviceTag, "Is not running");
        return false;
    }

    void resetPaymentService() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                try {
                    Globals.ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null).getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    Globals.mService = mService;
                    Log.d("BOUGHT_ITEMS", String.valueOf(Globals.ownedItems));

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
