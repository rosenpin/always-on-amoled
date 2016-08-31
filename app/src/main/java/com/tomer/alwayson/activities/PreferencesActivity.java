package com.tomer.alwayson.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.tomer.alwayson.BuildConfig;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SecretConstants;
import com.tomer.alwayson.SettingsFragment;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.receivers.DAReceiver;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.services.StarterService;

import java.util.ArrayList;

import eu.chainfire.libsuperuser.Shell;
import fr.nicolaspomepuy.discreetapprate.AppRate;

public class PreferencesActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, ContextConstatns {
    Prefs prefs;
    Intent billingServiceIntent;
    boolean isActive;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private boolean demo;

    public static void promptToSupport(final Activity context, final IInAppBillingService mService, final View rootView, boolean supporterFeature) {
        if (mService != null)
            new MaterialDialog.Builder(context)
                    .title(R.string.action_support_the_development)
                    .content(supporterFeature ? R.string.supporter_feature_only : R.string.support_how_much)
                    .items(R.array.support_items)
                    .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
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
                                                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
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
                                                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
                                            } catch (RemoteException e) {
                                                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                e.printStackTrace();
                                            }
                                            break;
                                    }
                                    if (pendingIntent != null)
                                        context.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                                    else
                                        Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error), Snackbar.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error), Snackbar.LENGTH_LONG).show();
                                }

                                return true;
                            }
                    ).show();
        else
            Toast.makeText(context, R.string.error_IAP, Toast.LENGTH_LONG).show();
    }

    public static void quicklyPromptToSupport(final Activity context, final IInAppBillingService mService, final View rootView) {
        if (mService != null) {
            String googleIAPCode = SecretConstants.getPropertyValue(context, "googleIAPCode");
            Bundle buyIntentBundle;
            PendingIntent pendingIntent = null;
            String IAPID = SecretConstants.getPropertyValue(context, "IAPID");
            try {
                buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                        IAPID, "inapp", googleIAPCode);
                pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                if (pendingIntent == null)
                    Snackbar.make(rootView, context.getString(R.string.thanks), Snackbar.LENGTH_LONG).show();
            } catch (RemoteException e) {
                Snackbar.make(rootView, context.getString(R.string.error_3_unknown_error_restart) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            try {
                if (pendingIntent != null)
                    context.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                else
                    Snackbar.make(rootView, context.getString(R.string.error_3_unknown_error_restart), Snackbar.LENGTH_LONG).show();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                Snackbar.make(rootView, context.getString(R.string.error_3_unknown_error_restart), Snackbar.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(context, R.string.error_IAP, Toast.LENGTH_LONG).show();
    }

    public static void uninstall(Context context, Prefs prefs) {
        try {
            ComponentName devAdminReceiver = new ComponentName(context, DAReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.removeActiveAdmin(devAdminReceiver);
            if (prefs.proximityToLock && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !Shell.SU.available())
                prefs.setBool(Prefs.KEYS.PROXIMITY_TO_LOCK.toString(), false);
        } catch (Exception ignored) {
        }
        Uri packageUri = Uri.parse("package:" + context.getPackageName());
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
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
            getFragmentManager().beginTransaction()
                    .replace(R.id.preferences_holder, new SettingsFragment())
                    .commit();

            handlePermissions();

            Intent starterServiceIntent = new Intent(getApplicationContext(), StarterService.class);

            donateButtonSetup();

            findViewById(R.id.preview).setOnClickListener(view -> {
                demo = true;
                Intent demoService = new Intent(getApplicationContext(), MainService.class);
                demoService.putExtra("demo", true);
                startService(demoService);
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
        donateButton.setOnClickListener(view -> PreferencesActivity.promptToSupport(PreferencesActivity.this, mService, findViewById(android.R.id.content), false));

    }

    private void handlePermissions() {
        if (Utils.isAndroidNewerThanM()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        123);
            }
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        try {
            View view = new View(getApplicationContext());
            ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(view, lp);
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);

            if (Utils.isAndroidNewerThanM()) {
                if (!Settings.System.canWrite(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            if (Utils.isAndroidNewerThanM()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
                startActivity(new Intent(getApplicationContext(), ReporterActivity.class));
                return true;
            case R.id.uninstall:
                uninstall(this, prefs);
                return true;
            case R.id.community:
                Utils.openURL(this, "https://plus.google.com/communities/104206728795122451273");
                return true;
            case R.id.github:
                Utils.openURL(this, "https://github.com/rosenpin/AlwaysOnDisplayAmoled");
                return true;
            case R.id.translate:
                Utils.openURL(this, "https://crowdin.com/project/always-on-amoled");
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
        Utils.logDebug(ContextConstatns.MAIN_SERVICE_LOG_TAG, String.valueOf(selectedColor));
        prefs.setInt(Prefs.KEYS.TEXT_COLOR.toString(), selectedColor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Utils.logDebug("Purchase state", String.valueOf(resultCode));
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.thanks, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.thanks, 10000).setAction(R.string.action_restart, view -> {
                    finish();
                    startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                }).show();
                resetPaymentService();
                Utils.logDebug("User bought item", data.getStringExtra("INAPP_PURCHASE_DATA"));
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

    @Override
    protected void onStop() {
        super.onStop();
        Utils.logDebug(MAIN_ACTIVITY_LOG_TAG, "Stopped");
        new Handler().postDelayed(() -> {
            if (!isActive)
                finish();
        }, 30000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        String TAG = serviceClass.getSimpleName();
        String serviceTag = serviceClass.getSimpleName();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Utils.logDebug(TAG, "Is already running");
                return true;
            }
        }
        Utils.logDebug(serviceTag, "Is not running");
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
                    Utils.logDebug("BOUGHT_ITEMS", String.valueOf(Globals.ownedItems));
                    if (BuildConfig.DEBUG)
                        Globals.ownedItems = new ArrayList<String>() {{
                            add("ITEM");
                        }};
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        billingServiceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        billingServiceIntent.setPackage("com.android.vending");
        try {
            unbindService(mServiceConn);
        } catch (Exception ignored) {
        }
        bindService(billingServiceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }
}
