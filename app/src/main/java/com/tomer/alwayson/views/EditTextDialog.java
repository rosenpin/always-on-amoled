package com.tomer.alwayson.views;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

public class EditTextDialog extends EditTextPreference {

    public EditTextDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        String text = getText();
        if (TextUtils.isEmpty(text))
            return getEditText().getHint();
        else {
            CharSequence summary = super.getSummary();
            if (summary != null)
                return String.format(summary.toString(), text);
            else
                return null;
        }
    }
}
