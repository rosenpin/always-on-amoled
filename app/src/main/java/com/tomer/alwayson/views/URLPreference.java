package com.tomer.alwayson.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;

public class URLPreference extends Preference {

    public URLPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.url_preference);
        CharSequence url = a.getString(R.styleable.url_preference_url);
        a.recycle();
        setOnPreferenceClickListener(preference -> {
            Utils.openURL(context, (String) url);
            return true;
        });
    }

}
