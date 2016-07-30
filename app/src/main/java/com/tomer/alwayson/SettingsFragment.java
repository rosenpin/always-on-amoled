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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.tasomaniac.android.widget.IntegrationPreference;
import com.tomer.alwayson.Activities.PreferencesActivity;
import com.tomer.alwayson.Receivers.DAReceiver;
import com.tomer.alwayson.Services.StarterService;
import com.tomer.alwayson.Views.FontAdapter;
import com.tomer.alwayson.Views.SeekBarPreference;

import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30;
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

    public static void openURL(String url, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    public static void openPlayStoreUrl(String appName, Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
        } catch (Exception e) {
            openURL("https://play.google.com/store/apps/details?id=" + appName, context);
        }
    }

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
        findPreference("font").setOnPreferenceClickListener(this);
        findPreference("watchface_clock").setOnPreferenceChangeListener(this);
        String[] preferencespList = {DOUBLE_TAP, SWIPE_UP, VOLUME_KEYS, BACK_BUTTON};
        for (String preference : preferencespList) {
            findPreference(preference).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d("Object value ", (String) o);
                    if (o.equals("speak")) {
                        if (Globals.ownedItems != null) {
                            if (Globals.ownedItems.size() > 0) {
                                if (!isPackageInstalled("com.google.android.tts")) {
                                    openURL("https://play.google.com/store/apps/details?id=com.google.android.tts", getActivity());
                                    Toast.makeText(context, R.string.warning_10_tts_not_installed, Toast.LENGTH_SHORT).show();
                                }
                                Log.d("Purchased items", String.valueOf(Globals.ownedItems));
                                return true;
                            } else {
                                PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
                            }
                        } else {
                            PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
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
            if (!isPackageInstalled("tomer.com.alwaysonamoledplugin") && android.os.Build.MANUFACTURER.toLowerCase().contains("samsung")) { //Prompt to install the plugin
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.plugin_dialog_title))
                        .setMessage(getString(R.string.plugin_dialog_desc))
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                openPlayStoreUrl("tomer.com.alwaysonamoledplugin", context);
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
                openURL("https://crowdin.com/project/always-on-amoled", getActivity());
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
                notices.addNotice(new Notice("android-issue-reporter", "https://github.com/HeinrichReimer/android-issue-reporter", "", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Custom Analog Clock View", "https://github.com/rosenpin/custom-analog-clock-view", "Copyright (C) 2016 Tomer Rosenfeld", new GnuGeneralPublicLicense30()));
                notices.addNotice(new Notice("IntegrationPreference", "https://github.com/tasomaniac/IntegrationPreference", "", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("LicensesDialog", "https://github.com/PSDev/LicensesDialog", "", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
                new LicensesDialog.Builder(getActivity())
                        .setNotices(notices)
                        .build()
                        .show();
                return true;
            }
        });
    }

    private void googlePlusCommunitySetup() {
        findPreference("community").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openURL("https://plus.google.com/communities/104206728795122451273", getActivity());
                return false;
            }
        });
    }

    private void githubLink() {
        findPreference("github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openURL("https://github.com/rosenpin/AlwaysOnDisplayAmoled", getActivity());
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
        if (preference.getKey().equals("watchface_clock")) {
            int value = Integer.parseInt((String) o);
            if (value > 2) {
                if (Globals.ownedItems != null) {
                    if (Globals.ownedItems.size() > 0) {
                        return true;
                    } else {
                        PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
                        return false;
                    }
                } else {
                    PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
                }
            } else {
                return true;
            }
        }

        prefs.apply();
        Log.d("Preference change", preference.getKey() + " Value:" + o.toString());

        if (preference.getKey().equals("notifications_alerts")) {
            if ((boolean) o) {
                return checkNotificationsPermission(context, true);
            }
            return true;
        }
        if (preference.getKey().equals("persistent_notification") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_1_harm_performance, 10000).setAction(R.string.action_revert, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckBoxPreference) preference).setChecked(true);
                    restartService();
                }
            }).show();
            restartService();
        }
        if (preference.getKey().equals("enabled")) {
            restartService();
        }
        if (preference.getKey().equals("proximity_to_lock")) {
            if (Shell.SU.available() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                return true;
            else {
                DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                final ComponentName mAdminName = new ComponentName(context, DAReceiver.class);
                if ((mDPM != null && mDPM.isAdminActive(mAdminName))) {
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
            }
        } else if (preference.getKey().equals("startafterlock") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_4_device_not_secured, 10000).setAction(R.string.action_revert, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckBoxPreference) preference).setChecked(true);
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
            PreferencesActivity.uninstall(context, prefs);
        } else if (preference.getKey().equals("font")) {
            final FontAdapter fontAdapter = new FontAdapter(context, R.array.fonts);
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.settings_choose_font)
                    .backgroundColor(Color.BLACK)
                    .titleColor(Color.WHITE)
                    .adapter(fontAdapter, new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (which > 5) {
                                if (Globals.ownedItems != null) {
                                    if (Globals.ownedItems.size() > 0) {
                                        prefs.setString("font", String.valueOf(which));
                                        dialog.dismiss();
                                    } else
                                        PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
                                } else {
                                    PreferencesActivity.promptToSupport(getActivity(), Globals.mService, rootView, true);
                                }
                            } else {
                                prefs.setString("font", String.valueOf(which));
                                dialog.dismiss();
                            }
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(MAIN_SERVICE_LOG_TAG, "Activity result" + resultCode);
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE)
            ((SwitchPreference) findPreference("proximity_to_lock")).setChecked(resultCode == Activity.RESULT_OK);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldEnableNotificationsAlerts && checkNotificationsPermission(context, false)) {
            ((TwoStatePreference) findPreference("notifications_alerts")).setChecked(true);
        }
        ((IntegrationPreference) findPreference("greenify")).resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((IntegrationPreference) findPreference("greenify")).pause();
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
