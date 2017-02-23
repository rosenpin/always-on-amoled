package com.tomer.alwayson;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.tomer.alwayson.activities.DonateActivity;
import com.tomer.alwayson.activities.Picker;
import com.tomer.alwayson.activities.PreferencesActivity;
import com.tomer.alwayson.activities.ReporterActivity;
import com.tomer.alwayson.helpers.DozeManager;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.receivers.DAReceiver;
import com.tomer.alwayson.services.StarterService;
import com.tomer.alwayson.views.FontAdapter;
import com.tomer.alwayson.views.SeekBarPreference;

import java.io.IOException;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import eu.chainfire.libsuperuser.Shell;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener, ContextConstatns {

    boolean shouldEnableNotificationsAlerts;
    private View rootView;
    private Prefs prefs;
    private Context context;
    private Intent starterService;
    private ComponentName mAdminName;

    public static void openPlayStoreUrl(String appName, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
        } catch (Exception e) {
            Utils.openURL(context, "https://play.google.com/store/apps/details?id=" + appName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        context = getActivity().getApplicationContext();
        mAdminName = new ComponentName(context, DAReceiver.class);
        prefs = new Prefs(context);
        prefs.apply();
        findPreference("enabled").setOnPreferenceChangeListener(this);
        findPreference("persistent_notification").setOnPreferenceChangeListener(this);
        findPreference("raise_to_wake").setOnPreferenceChangeListener(this);
        findPreference("greenify_enabled").setOnPreferenceChangeListener(this);
        findPreference("proximity_to_lock").setOnPreferenceChangeListener(this);
        findPreference("startafterlock").setOnPreferenceChangeListener(this);
        findPreference("notifications_alerts").setOnPreferenceChangeListener(this);
        findPreference("doze_mode").setOnPreferenceChangeListener(this);
        findPreference("google_now_shortcut").setOnPreferenceChangeListener(this);
        findPreference("camera_shortcut").setOnPreferenceChangeListener(this);
        findPreference("stop_delay").setOnPreferenceChangeListener(this);
        findPreference("battery_saver").setOnPreferenceChangeListener(this);
        findPreference("watchface_clock").setOnPreferenceClickListener(this);
        findPreference("watchface_date").setOnPreferenceClickListener(this);
        findPreference("textcolor").setOnPreferenceClickListener(this);
        findPreference("uninstall").setOnPreferenceClickListener(this);
        findPreference("font").setOnPreferenceClickListener(this);
        ((SeekBarPreference) findPreference("font_size")).setMin(20);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        String[] gesturePreferencesList = {DOUBLE_TAP, SWIPE_UP, SWIPE_DOWN, VOLUME_KEYS, BACK_BUTTON};
        for (String preference : gesturePreferencesList) {
            findPreference(preference).setOnPreferenceChangeListener((preference1, o) -> {
                switch (Integer.parseInt((String) o)) {
                    case DISABLED:
                        return true;
                    case ACTION_UNLOCK:
                        return true;
                    case ACTION_SPEAK:
                        if (isSupporter()) {
                            if (!isPackageInstalled("com.google.android.tts"))
                                Utils.openURL(getActivity(), "https://play.google.com/store/apps/details?id=com.google.android.tts");
                            return true;
                        } else {
                            DonateActivity.quicklyPromptToSupport(getActivity(), rootView);
                            return false;
                        }
                    case ACTION_FLASHLIGHT:
                        if (isSupporter()) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                                return false;
                            }
                            return true;
                        } else {
                            DonateActivity.quicklyPromptToSupport(getActivity(), rootView);
                            return false;
                        }
                }
                return true;
            });
        }
        checkNotificationsPermission(context, false);
        starterService = new Intent(getActivity().getApplicationContext(), StarterService.class);
        Utils.logDebug(String.valueOf(((MaterialListPreference) findPreference("rules")).getValue()), " Selected");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    private boolean isSupporter() {
        if (Globals.ownedItems != null) {
            Utils.logDebug("Purchased items", String.valueOf(Globals.ownedItems));
            return Globals.ownedItems.size() > 0;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = getView();
        assert rootView != null;
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);
        prefs = new Prefs(context);
        prefs.apply();
        if (hasSoftKeys()) {
            findPreference(BACK_BUTTON).setEnabled(false);
        } else {
            if (!prefs.neverShowPluginDialog) {
                if (!isPackageInstalled("tomer.com.alwaysonamoledplugin")) { //Prompt to install the plugin
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setTitle(getString(R.string.plugin_dialog_title))
                            .setMessage(getString(R.string.plugin_dialog_desc))
                            .setPositiveButton("Download", (dialogInterface, i) -> {
                                openPlayStoreUrl("tomer.com.alwaysonamoledplugin", context);
                                dialogInterface.dismiss();
                            })
                            .setCancelable(false);
                    if (prefs.showedPluginDialog)
                        alertDialogBuilder.setNeutralButton(getString(R.string.never_show_again), (dialogInterface, i) -> prefs.setBool(Prefs.KEYS.NEVER_SHOW_DIALOG.toString(), true));
                    alertDialogBuilder.show();
                    prefs.setBool(Prefs.KEYS.SHOWED_DIALOG.toString(), true);
                }
            }
        }
        if (!Utils.isSamsung(context)) {
            PreferenceScreen gesturesPrefs = (PreferenceScreen) findPreference("gestures_prefs");
            PreferenceCategory samsungPrefs = (PreferenceCategory) findPreference("samsung_prefs");
            gesturesPrefs.removePreference(samsungPrefs);
        }
        version(context);
        openSourceLicenses();
    }

    private void setUpBatterySaverPermission() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm grant " + getActivity().getPackageName() + " android.permission.WRITE_SECURE_SETTINGS"});
            process.waitFor();
        } catch (IOException | InterruptedException ignored) {
            Log.i(MAIN_ACTIVITY_LOG_TAG, "User doesn't have root");
        }
    }

    private void restartService() {
        getActivity().stopService(starterService);
        getActivity().startService(starterService);
    }

    private void version(Context c) {
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            assert findPreference("version") != null;
            findPreference("version").setSummary(getString(R.string.settings_app_version) + ": " + pInfo.versionName + " " + getString(R.string.settings_version_desc) + ": " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final int[] counter = {0};
        findPreference("version").setOnPreferenceClickListener(preference -> {
            counter[0]++;
            if (counter[0] >= 5) {
                Intent intent = new Intent(context, ReporterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            return false;
        });
    }

    private void openSourceLicenses() {
        findPreference("open_source_licenses").setOnPreferenceClickListener(preference -> {
            Notices notices = new Notices();
            notices.addNotice(new Notice("AppIntro", "https://github.com/PaoloRotolo/AppIntro", "Copyright 2015 Paolo Rotolo ,  Copyright 2016 Maximilian Narr", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("android-issue-reporter", "https://github.com/HeinrichReimer/android-issue-reporter", "", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("ButterKnife", "https://github.com/JakeWharton/butterknife", "Copyright 2013 Jake Wharton", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("Custom Analog Clock View", "https://github.com/rosenpin/custom-analog-clock-view", "Copyright (C) 2016 Tomer Rosenfeld", new GnuGeneralPublicLicense30()));
            notices.addNotice(new Notice("CircleImageView", "https://github.com/hdodenhof/CircleImageView", "Copyright 2014 - 2016 Henning Dodenhof", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("LicensesDialog", "https://github.com/PSDev/LicensesDialog", "", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
            new LicensesDialog.Builder(getActivity())
                    .setNotices(notices)
                    .build()
                    .show();
            return true;
        });
    }

    private boolean checkNotificationsPermission(Context c, boolean prompt) {
        ContentResolver contentResolver = c.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = c.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            ((SwitchPreference) findPreference("notifications_alerts")).setChecked(false);
            if (Utils.isAndroidNewerThanL() && prompt) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                checkAndStartActivity(intent);
                shouldEnableNotificationsAlerts = true;
            } else if (prompt) {
                checkAndStartActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                shouldEnableNotificationsAlerts = true;
            }
            return false;
        }
        return true;
    }

    private void checkAndStartActivity(Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0)
            startActivity(intent);
    }

    public boolean isPackageInstalled(String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }

    private boolean hasSoftKeys() {
        boolean hasSoftwareKeys;

        Display d = getActivity().getWindowManager().getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;

        prefs.setBool(Prefs.KEYS.HAS_SOFT_KEYS.toString(), hasSoftwareKeys);

        return hasSoftwareKeys;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object o) {
        prefs.apply();
        Utils.logDebug("Preference change", preference.getKey() + " Value:" + o.toString());

        if (preference.getKey().equals("notifications_alerts")) {
            if ((boolean) o)
                return checkNotificationsPermission(context, true);
            return true;
        }
        if (preference.getKey().equals("raise_to_wake")) {
            if (!Utils.hasFingerprintSensor(context))
                askDeviceAdmin(R.string.settings_raise_to_wake_device_admin);
            restartService();
        }
        if (preference.getKey().equals("stop_delay"))
            if (!Utils.hasFingerprintSensor(context))
                askDeviceAdmin(R.string.settings_raise_to_wake_device_admin);
        if (preference.getKey().equals("persistent_notification") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_1_harm_performance, 10000).setAction(R.string.action_revert, v -> {
                ((CheckBoxPreference) preference).setChecked(true);
                restartService();
            }).show();
            restartService();
        }
        if (preference.getKey().equals("enabled")) {
            context.sendBroadcast(new Intent(TOGGLED));
            restartService();
        }
        if (preference.getKey().equals("proximity_to_lock")) {
            if (Shell.SU.available() || (Utils.isAndroidNewerThanL() && !Build.MANUFACTURER.equalsIgnoreCase("samsung")))
                return true;
            else {
                DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if ((mDPM != null && mDPM.isAdminActive(mAdminName))) {
                    return true;
                }
                new AlertDialog.Builder(getActivity()).setTitle(getString(android.R.string.dialog_alert_title) + "!")
                        .setMessage(getString(R.string.warning_7_disable_fingerprint))
                        .setPositiveButton(getString(android.R.string.yes), (dialogInterface, i) -> {
                            askDeviceAdmin(R.string.settings_raise_to_wake_device_admin);
                        })
                        .setNegativeButton(getString(android.R.string.no), (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .show();
                return false;
            }
        }
        if (preference.getKey().equals("startafterlock") && !(boolean) o)
            Snackbar.make(rootView, R.string.warning_4_device_not_secured, 10000).setAction(R.string.action_revert, v -> ((CheckBoxPreference) preference).setChecked(true)).show();
        if (preference.getKey().equals("doze_mode") && (boolean) o) {
            if (Shell.SU.available()) {
                if (!DozeManager.isDumpPermissionGranted(context))
                    DozeManager.grantPermission(context, "android.permission.DUMP");
                if (!DozeManager.isDevicePowerPermissionGranted(context))
                    DozeManager.grantPermission(context, "android.permission.DEVICE_POWER");
                return true;
            }
            Snackbar.make(rootView, R.string.warning_11_no_root, Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (preference.getKey().equals("greenify_enabled") && (boolean) o) {
            if (!isPackageInstalled("com.oasisfeng.greenify")) {
                openPlayStoreUrl("com.oasisfeng.greenify", context);
                return false;
            }
        }
        if (preference.getKey().equals("camera_shortcut") || preference.getKey().equals("google_now_shortcut")) {
            try {
                if (!hasUsageAccess()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    PackageManager packageManager = getActivity().getPackageManager();
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(context, "Please grant usage access permission manually for the app, your device can't do it automatically.", Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (preference.getKey().equals("battery_saver"))
            if ((boolean) o) {
                ((TwoStatePreference) findPreference("doze_mode")).setChecked(true);
                setUpBatterySaverPermission();
            }
        return true;
    }

    private void askDeviceAdmin(@StringRes int message) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(message));
        startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);
    }

    private boolean hasUsageAccess() throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } else
            return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateSpecialPreferences();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("textcolor")) {
            if (Globals.colorDialog != null)
                Globals.colorDialog.show();
            else
                Snackbar.make(rootView, R.string.error_3_unknown_error_restart, Snackbar.LENGTH_LONG).setAction(R.string.action_restart, v -> {
                    getActivity().finish();
                    context.startActivity(new Intent(context, PreferencesActivity.class));
                }).show();
        } else if (preference.getKey().equals("uninstall")) {
            Utils.logDebug(MAIN_ACTIVITY_LOG_TAG, "uninstall clicked");
            PreferencesActivity.uninstall(context, prefs);
        } else if (preference.getKey().equals("font")) {
            final FontAdapter fontAdapter = new FontAdapter(context, R.array.fonts);
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.settings_choose_font)
                    .backgroundColor(Color.BLACK)
                    .titleColor(Color.WHITE)
                    .adapter(fontAdapter, (dialog, itemView, which, text) -> {
                        if (which > 5) {
                            if (Globals.ownedItems != null) {
                                if (Globals.ownedItems.size() > 0) {
                                    prefs.setString(Prefs.KEYS.FONT.toString(), String.valueOf(which));
                                    dialog.dismiss();
                                } else
                                    DonateActivity.quicklyPromptToSupport(getActivity(), rootView);
                            } else {
                                DonateActivity.quicklyPromptToSupport(getActivity(), rootView);
                            }
                        } else {
                            prefs.setString("font", String.valueOf(which));
                            dialog.dismiss();
                        }
                    })
                    .show();
            return false;
        } else if (preference.getKey().equals("watchface_clock")) {
            Intent intent = new Intent(context, Picker.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(GRID_TYPE, GRID_TYPE_CLOCK);
            context.startActivity(intent);
            return false;
        } else if (preference.getKey().equals("watchface_date")) {
            Intent intent = new Intent(context, Picker.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(GRID_TYPE, GRID_TYPE_DATE);
            context.startActivity(intent);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.logDebug(MAIN_SERVICE_LOG_TAG, "Activity result" + resultCode);
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE)
            ((CheckBoxPreference) findPreference("proximity_to_lock")).setChecked(resultCode == Activity.RESULT_OK);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSpecialPreferences();
    }

    private void updateSpecialPreferences() {
        if (shouldEnableNotificationsAlerts && checkNotificationsPermission(context, false)) {
            ((TwoStatePreference) findPreference("notifications_alerts")).setChecked(true);
        }
        if (((MaterialListPreference) findPreference("stop_delay")).getValue().equals("0"))
            findPreference("stop_delay").setSummary(R.string.settings_stop_delay_desc);
        else
            findPreference("stop_delay").setSummary("%s");
        findPreference("watchface_clock").setSummary(context.getResources().getStringArray(R.array.customize_clock)[prefs.clockStyle]);
        findPreference("watchface_date").setSummary(context.getResources().getStringArray(R.array.customize_date)[prefs.dateStyle]);
        findPreference("greenify_enabled").setSummary(isPackageInstalled("com.oasisfeng.greenify") ? context.getString(R.string.settings_greenify_integration_desc) : context.getString(R.string.settings_greenify_integration_desc_not_found));
        if (!isPackageInstalled("com.oasisfeng.greenify")) {
            ((SwitchPreference) findPreference("greenify_enabled")).setChecked(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void temporaryUnusedFunctionToSetAppToOpen() {
        //Todo fix open app/shortcut from service
        /*
        for (final String KEY : wakeUpList) {
            findPreference(KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) { //Support for opening apps/shortcuts after gesture.
                     if (o.toString().equals("open_app")) {
                        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                        List<ResolveInfo> shortcuts = context.getPackageManager().queryIntentActivities(shortcutsIntent, 0);
                        final String[][] apps = new String[3][shortcuts.size()];
                        for (int i = 0; i < shortcuts.size(); i++) {
                            apps[0][i] = (String) shortcuts.get(i).loadLabel(context.getPackageManager());
                            apps[1][i] = shortcuts.get(i).activityInfo.targetActivity;
                            apps[2][i] = shortcuts.get(i).activityInfo.packageName;
                        }
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.settings_gestures_select_app)
                                .items(apps[0])
                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName(apps[2][which], apps[1][which]));
                                        startActivityForResult(intent, 5);
                                        if (view != null) {
                                            prefs.setString(KEY + "_app", apps[1][which]);
                                            Utils.logDebug("Selected shortcut ", apps[1][which]);
                                        }
                                        return true;
                                    }
                                })
                                .positiveText(android.R.string.ok)
                                .show();
                    } else {
                        prefs.getSharedPrefs().edit().remove(KEY + "_app").apply();
                    }
                    return true;
                }
            });
        }*/
    }
}
