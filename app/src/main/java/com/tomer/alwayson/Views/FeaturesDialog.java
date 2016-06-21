package com.tomer.alwayson.Views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;

public class FeaturesDialog extends Dialog {
    CheckBox[] checkboxes;

    public FeaturesDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.feature_dialog);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        final Prefs prefs = new Prefs(getContext());
        prefs.apply();
        checkboxes = new CheckBox[]{(CheckBox) findViewById(R.id.dialog_time), (CheckBox) findViewById(R.id.dialog_date), (CheckBox) findViewById(R.id.dialog_battery)};
        boolean toCheck = true;
        for (int i = 0; i < checkboxes.length; i++) {
            String key = "";
            switch (i) {
                case 0:
                    key = Prefs.KEYS.SHOW_TIME.toString();
                    toCheck = prefs.showTime;
                    break;
                case 1:
                    key = Prefs.KEYS.SHOW_DATE.toString();
                    toCheck = prefs.showDate;
                    break;
                case 2:
                    key = Prefs.KEYS.SHOW_BATTERY.toString();
                    toCheck = prefs.showBattery;
                    break;
            }
            checkboxes[i].setChecked(toCheck);
            final String finalKey = key;
            checkboxes[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    prefs.setBool(finalKey, isChecked);
                }
            });
        }
        findViewById(R.id.dialog_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
