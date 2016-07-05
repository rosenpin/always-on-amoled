package com.tomer.alwayson;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tomer.alwayson.Activities.PreferencesActivity;
import com.tomer.alwayson.Receivers.DAReceiver;
import com.tomer.alwayson.Services.StarterService;
import com.tomer.alwayson.Views.SeekBarPreference;

import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import eu.chainfire.libsuperuser.Shell;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, ContextConstatns {

    boolean shouldEnableNotificationsAlerts;
    private View rootView;
    private Prefs prefs;
    private Context context;
    private Intent starterService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        context = getActivity().getApplicationContext();
        findPreference("enabled").setOnPreferenceChangeListener(this);
        findPreference("persistent_notification").setOnPreferenceChangeListener(this);
        findPreference("proximity_to_lock").setOnPreferenceChangeListener(this);
        findPreference("startafterlock").setOnPreferenceChangeListener(this);
        findPreference("notifications_alerts").setOnPreferenceChangeListener(this);
        findPreference("textcolor").setOnPreferenceClickListener(this);
        ((SeekBarPreference) findPreference("font_size")).setMin(20);
        findPreference("uninstall").setOnPreferenceClickListener(this);
        String[] preferencespList = {DOUBLE_TAP, SWIPE_UP, VOLUME_KEYS, BACK_BUTTON};
        for (String preference : preferencespList) {
            findPreference(preference).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d("Object value ", (String) o);
                    if (o.equals("speak")) {
                        if (Globals.ownedItems.size() > 0) {
                            if (!isPackageInstalled("com.google.android.tts", context)) {
                                openURL("https://play.google.com/store/apps/details?id=com.google.android.tts");
                                Toast.makeText(context, R.string.warning_10_tts_not_installed, Toast.LENGTH_SHORT).show();
                            }
                            Log.d("Purchased items", String.valueOf(Globals.ownedItems));
                            return true;
                        } else {
                            PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView);
                        }
                        return false;
                    }
                    return true;
                }
            });
        }
        checkNotificationsPermission(context, false);
        starterService = new Intent(getActivity().getApplicationContext(), StarterService.class);
        Log.d(String.valueOf(((ListPreference) findPreference("rules")).getValue()), " Selected");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);
        prefs = new Prefs(context);
        if (hasSoftKeys()) {
            findPreference("back_button").setEnabled(false);
        } else {
            if (!IsPackageInstalled("tomer.com.alwaysonamoledplugin") && android.os.Build.MANUFACTURER.toLowerCase().contains("samsung")) { //Prompt to install the plugin
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.plugin_dialog_title))
                        .setMessage(getString(R.string.plugin_dialog_desc))
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=tomer.com.alwaysonamoledplugin"));
                                startActivity(browserIntent);
                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
        version(context);
        translate();
        googlePlusCommunitySetup();
        openSourceLicenses();
        githubLink();
    }

    private void restartService() {
        getActivity().stopService(starterService);
        getActivity().startService(starterService);
    }

    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void version(Context c) {
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            assert findPreference("version") != null;
            findPreference("version").setSummary(getString(R.string.settings_app_version) + ": " + pInfo.versionName + " " + getString(R.string.settings_version_desc) + ": " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void translate() {
        findPreference("translate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openURL("https://crowdin.com/project/always-on-amoled");
                return false;
            }
        });
    }

    private void openSourceLicenses() {
        findPreference("open_source_licenses").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Notices notices = new Notices();
                notices.addNotice(new Notice("AppIntro", "https://github.com/PaoloRotolo/AppIntro", "Copyright 2015 Paolo Rotolo ,  Copyright 2016 Maximilian Narr", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
                notices.addNotice(new Notice("LicensesDialog", "https://github.com/PSDev/LicensesDialog", "", new ApacheSoftwareLicense20()));
                new LicensesDialog.Builder(getActivity())
                        .setNotices(notices)
                        .build()
                        .show();
                return false;
            }
        });
    }

    private void googlePlusCommunitySetup() {
        findPreference("community").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openURL("https://plus.google.com/communities/104206728795122451273");
                return false;
            }
        });
    }

    private void githubLink() {
        findPreference("github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openURL("https://github.com/rosenpin/AlwaysOnDisplayAmoled");
                return false;
            }
        });
    }

    private boolean checkNotificationsPermission(Context c, boolean prompt) {
        ContentResolver contentResolver = c.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = c.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            ((SwitchPreference) findPreference("notifications_alerts")).setChecked(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && prompt) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                shouldEnableNotificationsAlerts = true;
            } else if (prompt) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                shouldEnableNotificationsAlerts = true;
            }
            return false;
        }
        return true;
    }

    public boolean IsPackageInstalled(String targetPackage) {
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

        prefs.setBool("has_soft_keys", hasSoftwareKeys);

        return hasSoftwareKeys;
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference origPreference, Object o) {
        final TwoStatePreference preference = (TwoStatePreference) origPreference;
        prefs.apply();
        Log.d("Preference change", preference.getKey() + " Value:" + o.toString());

        if (preference.getKey().equals("notifications_alerts")) {
            if (!preference.isChecked()) {
                return checkNotificationsPermission(context, true);
            }
            return true;
        }
        if (preference.getKey().equals("persistent_notification") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_1_harm_performance, 10000).setAction(R.string.action_revert, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preference.setChecked(true);
                    restartService();
                }
            }).show();
            restartService();
        }
        if (preference.getKey().equals("enabled")) {
            restartService();
        }
        if (preference.getKey().equals("proximity_to_lock") && (boolean) o) {
            DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            final ComponentName mAdminName = new ComponentName(context, DAReceiver.class);
            if (Shell.SU.available() || (mDPM != null && mDPM.isAdminActive(mAdminName))) {
                return true;
            }

            new AlertDialog.Builder(getActivity()).setTitle(getString(android.R.string.dialog_alert_title) + "!")
                    .setMessage(getString(R.string.warning_7_disable_fingerprint))
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_explanation));
                            startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return false;
        } else if (preference.getKey().equals("startafterlock") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_4_device_not_secured, 10000).setAction(R.string.action_revert, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preference.setChecked(true);
                }
            }).show();
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("textcolor")) {
            Globals.colorDialog.show();
        } else if (preference.getKey().equals("uninstall")) {
            Log.d(MAIN_ACTIVITY_LOG_TAG, "uninstall clicked");
            try {
                ComponentName devAdminReceiver = new ComponentName(context, DAReceiver.class);
                DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                dpm.removeActiveAdmin(devAdminReceiver);
            } catch (Exception ignored) {
            }
            Uri packageUri = Uri.parse("package:" + context.getPackageName());
            Intent uninstallIntent =
                    new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            startActivity(uninstallIntent);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(MAIN_SERVICE_LOG_TAG, "Activity result" + resultCode);
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE)
            ((TwoStatePreference) findPreference("proximity_to_lock")).setChecked(resultCode == Activity.RESULT_OK);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldEnableNotificationsAlerts && checkNotificationsPermission(context, false)) {
            ((TwoStatePreference) findPreference("notifications_alerts")).setChecked(true);
        }
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
                                            Log.d("Selected shortcut ", apps[1][which]);
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
