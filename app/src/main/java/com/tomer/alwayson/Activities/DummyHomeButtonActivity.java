package com.tomer.alwayson.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tomer.alwayson.HomeWatcher;
import com.tomer.alwayson.Services.MainService;

public class DummyHomeButtonActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Home activity started ", "yes");
        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                stopService(new Intent(getApplicationContext(), MainService.class));

                Log.d("Home button was ", "pressed");
            }

            @Override
            public void onHomeLongPressed() {
                stopService(new Intent(getApplicationContext(), MainService.class));

                Log.d("Home button was ", "long pressed");
            }
        });
        mHomeWatcher.startWatch();
    }
}
