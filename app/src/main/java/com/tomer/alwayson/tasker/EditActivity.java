package com.tomer.alwayson.tasker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.tasker.bundle.PluginBundleManager;


public final class EditActivity extends AppCompatActivity {
    public static final String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasker_config);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", "Start the always on service");
        ((RadioGroup) findViewById(R.id.tasker_radio_group)).setOnCheckedChangeListener((radioGroup, i) -> {
            Utils.logDebug("Selected", (String) ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText());
            finish();
        });
    }

    @Override
    public void finish() {
        String value = (String) ((RadioButton) findViewById(((RadioGroup) findViewById(R.id.tasker_radio_group)).getCheckedRadioButtonId())).getText();
        Utils.logDebug("Selected", value);
        Intent resultIntent = new Intent();
        Bundle resultBundle = PluginBundleManager.generateBundle(value);
        resultIntent.putExtra(EXTRA_BUNDLE, resultBundle);
        resultIntent.putExtra(EXTRA_STRING_BLURB, value);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }
}