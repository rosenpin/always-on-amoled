package com.tomer.alwayson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ListView;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private Intent widgetUpdaterService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("enabled").setOnPreferenceChangeListener(this);
        findPreference("persistent_notification").setOnPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDivider(null);

        if (hasSoftKeys())
            findPreference("back_button_dismiss").setEnabled(false);

        prefs = new Prefs(getActivity().getApplicationContext());
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

    Prefs prefs;

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        prefs.apply();
        Log.d("Preference change", preference.getKey() + " Value:" +  prefs.getByKey(preference.getKey(), true));

        if (preference.getKey().equals("notifications_alerts")){
            
        }
        return true;
    }
}