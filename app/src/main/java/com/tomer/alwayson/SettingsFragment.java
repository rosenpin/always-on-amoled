package com.tomer.alwayson;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tomer.alwayson.Services.StarterService;
import com.tomer.alwayson.Views.FeaturesDialog;

import java.util.Arrays;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import eu.chainfire.libsuperuser.Shell;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, ContextConstatns {

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
        findPreference("watchface").setOnPreferenceClickListener(this);
        findPreference("textcolor").setOnPreferenceClickListener(this);
        starterService = new Intent(getActivity().getApplicationContext(), StarterService.class);
        Log.d(String.valueOf(((ListPreference)findPreference("rules")).getValue())," Selected");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);

        prefs = new Prefs(context);

        if (hasSoftKeys()) {
            findPreference("back_button_dismiss").setEnabled(false);
            ((CheckBoxPreference) findPreference("back_button_dismiss")).setChecked(false);
        }

        findPreference("notifications_alerts").setOnPreferenceChangeListener(this);

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
            findPreference("version").setSummary(getString(R.string.app_version) + ": " + pInfo.versionName + " " + getString(R.string.build) + ": " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void translate() {
        findPreference("translate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tomerrosenfeld.oneskyapp.com/collaboration/project/158837"));
                startActivity(browserIntent);
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/104206728795122451273"));
                startActivity(browserIntent);
                return false;
            }
        });
    }

    private void githubLink() {
        findPreference("github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rosenpin/AlwaysOnDisplayAmoled"));
                startActivity(browserIntent);
                return false;
            }
        });
    }

    private boolean checkAndGrantNotificationsPermission(Context c) {
        ContentResolver contentResolver = c.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = c.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            ((SwitchPreference) findPreference("notifications_alerts")).setChecked(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return false;
            }
        }
        return true;
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

        return hasSoftwareKeys;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object o) {
        prefs.apply();
        Log.d("Preference change", preference.getKey() + " Value:" + o.toString());

        if (preference.getKey().equals("notifications_alerts")) {
            if (!((SwitchPreference) preference).isChecked()) {
                return checkAndGrantNotificationsPermission(context);
            }
        }
        if (preference.getKey().equals("persistent_notification") && !(boolean) o) {
            Snackbar.make(rootView, R.string.warning_1_harm_performance, 10000).setAction(R.string.revert, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (preference instanceof CheckBoxPreference)
                        ((CheckBoxPreference) preference).setChecked(true);
                    restartService();
                }
            }).show();
            restartService();
        }
        if (preference.getKey().equals("enabled")) {
            restartService();
        }
        if (preference.getKey().equals("proximity_to_lock") && (boolean) o) {
            if (Shell.SU.available()) {
                return true;
            }
            Snackbar.make(rootView, "You currently need to be rooted for this feature", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("watchface")) {
            FeaturesDialog featuresDialog = new FeaturesDialog(getActivity());
            featuresDialog.setTitle(getString(R.string.settings_watchface));
            featuresDialog.show();
        } else if (preference.getKey().equals("textcolor")) {
            Globals.colorDialog.show();
        }
        return true;
    }
}