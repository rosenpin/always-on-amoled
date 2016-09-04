package com.tomer.alwayson.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.helpers.HomeWatcher;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.MainService;

public class SamsungHomeWatcherActivity extends AppCompatActivity implements ContextConstatns {
    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(finishBroadcastReceiver, new IntentFilter(FINISH_HOME_BUTTON_ACTIVITY));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        HomeWatcher homeWatcher = new HomeWatcher(this);
        homeWatcher.setOnHomePressedListener(() -> {
            finish();
            MainService.stoppedByShortcut = true;
            Utils.stopMainService(SamsungHomeWatcherActivity.this);
        });
        homeWatcher.startWatch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishBroadcastReceiver);
    }
}
