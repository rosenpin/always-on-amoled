package com.tomer.alwayson.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.tomer.alwayson.Globals;

public class DummySamsungActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().getInt("kill") == 1)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DummySamsungActivity.class.getCanonicalName(), "onresume");
        if (!Globals.isServiceRunning)
            finish();
    }
}
