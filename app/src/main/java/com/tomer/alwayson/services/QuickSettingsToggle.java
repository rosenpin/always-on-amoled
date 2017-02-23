package com.tomer.alwayson.services;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsToggle extends TileService implements ContextConstatns {
    private Prefs prefs;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Received change");
            setCurrentState(prefs.enabled ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
        }
    };
    private Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        Log("Tile Bind");
        this.intent = intent;
        registerReceiver(broadcastReceiver, new IntentFilter(TOGGLED));
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onTileAdded() {
        Log("Tile Added");
        super.onTileAdded();
        if (getQsTile() == null)
            onBind(intent);
        setCurrentState(getState());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setCurrentState(getState());
    }

    @Override
    public void onTileRemoved() {
        Log("Tile Removed");
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        super.onClick();
        Log("Clicked");
        initPrefs();
        Tile tile = getQsTile();
        if (tile != null) {
            switch (tile.getState()) {
                case Tile.STATE_ACTIVE:
                    prefs.setBool(Prefs.KEYS.ENABLED.toString(), false);
                    setCurrentState(Tile.STATE_INACTIVE);
                    break;
                case Tile.STATE_INACTIVE:
                    prefs.setBool(Prefs.KEYS.ENABLED.toString(), true);
                    setCurrentState(Tile.STATE_ACTIVE);
                    break;
                default:
                    tile.setLabel(getString(R.string.quick_settings_title) + " " + (prefs.enabled ? getString(R.string.quick_settings_service_active) : getString(R.string.quick_settings_service_inactive)));
                    Log("Active");
                    break;
            }
        } else {
            Log("Tile is null");
        }
    }

    private void setCurrentState(int state) {
        initPrefs();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(state);
            switch (state) {
                case Tile.STATE_ACTIVE:
                    tile.setLabel(getString(R.string.quick_settings_title) + " " + getString(R.string.quick_settings_service_active));
                    Log("Active");
                    break;
                case Tile.STATE_INACTIVE:
                    tile.setLabel(getString(R.string.quick_settings_title) + " " + getString(R.string.quick_settings_service_inactive));
                    Log("Inactive");
                    break;
                default:
                    tile.setLabel(getString(R.string.quick_settings_title) + " " + (prefs.enabled ? getString(R.string.quick_settings_service_active) : getString(R.string.quick_settings_service_inactive)));
                    Log("Active");
                    break;
            }
            tile.updateTile();
        }
    }

    private int getState() {
        initPrefs();
        return prefs.enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    private void initPrefs() {
        if (prefs == null)
            prefs = new Prefs(this);
        prefs.apply();
    }

    private void Log(String text) {
        Utils.logDebug(QuickSettingsToggle.class.getSimpleName(), text);
    }
}
