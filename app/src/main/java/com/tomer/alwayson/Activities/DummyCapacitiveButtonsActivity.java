package com.tomer.alwayson.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * Created by tomer on 6/11/16.
 */
public class DummyCapacitiveButtonsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DIMMING BUTTONS");
        setDimButtons(getIntent().getBooleanExtra("turn", true));
        finish();

    }

    private void setDimButtons(boolean dimButtons) {
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        float val = dimButtons ? 0 : -1;
        try {
            Field buttonBrightness = layoutParams.getClass().getField(
                    "buttonBrightness");
            buttonBrightness.set(layoutParams, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
        window.setAttributes(layoutParams);
    }
}
