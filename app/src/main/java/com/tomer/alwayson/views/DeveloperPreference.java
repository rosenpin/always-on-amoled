package com.tomer.alwayson.views;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import com.tomer.alwayson.activities.DeveloperActivity;

public class DeveloperPreference extends Preference {
    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(preference -> {
            context.startActivity(new Intent(context, DeveloperActivity.class));
            return false;
        });
    }
}
