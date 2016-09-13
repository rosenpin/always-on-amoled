package com.tomer.alwayson.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SettingsFragment;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.receivers.DAReceiver;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.services.QuickSettingsToggle;
import com.tomer.alwayson.services.StarterService;

import eu.chainfire.libsuperuser.Shell;
import fr.nicolaspomepuy.discreetapprate.AppRate;

public class PreferencesActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, ContextConstatns {
    private boolean isActive;
    private Prefs prefs;
    private boolean demo;

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
        DonateActivity.resetPaymentService(this);
        if (!prefs.permissionGranting) {
            startActivity(new Intent(getApplicationContext(), Intro.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            getFragmentManager().beginTransaction()
                    .replace(R.id.preferences_holder, new SettingsFragment())
                    .commitAllowingStateLoss();

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    private void appRate() {
        AppRate.with(this).starRating(true).starRatingListener(new AppRate.OnStarRateListener() {
            @Override
            public void onPositiveRating(int starRating) {
                Toast.makeText(PreferencesActivity.this, R.string.toast_thanks, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNegativeRating(int starRating) {
                Toast.makeText(PreferencesActivity.this, R.string.toast_thanks, Toast.LENGTH_LONG).show();
            }
        }).checkAndShow();
    }

    private void donateButtonSetup() {
        Button donateButton = (Button) findViewById(R.id.donate);
        assert donateButton != null;
        donateButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), DonateActivity.class)));
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
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        Utils.logDebug(ContextConstatns.MAIN_SERVICE_LOG_TAG, String.valueOf(selectedColor));
        prefs.setInt(Prefs.KEYS.TEXT_COLOR.toString(), selectedColor);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isServiceRunning(MainService.class)) {
            Utils.stopMainService(getApplicationContext());
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isServiceRunning(MainService.class) && demo) {
            Utils.stopMainService(getApplicationContext());
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
    protected void onDestroy() {
        super.onDestroy();
        Globals.colorDialog = null;
        Globals.ownedItems = null;
        DonateActivity.onDestroy(this);
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
}
