package com.tomer.alwayson.Services;

import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.tomer.alwayson.Helpers.Prefs;
import com.tomer.alwayson.R;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsToggle extends TileService {

    Prefs prefs;

    @Override
    public void onTileAdded() {
        Log("Tile Added");
        super.onTileAdded();
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
            }
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
            }
            tile.updateTile();
        }
    }

    private int getState() {
        initPrefs();
        return prefs.enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    private void initPrefs() {
        if (prefs == null) {
            prefs = new Prefs(this);
            prefs.apply();
        }
    }

    private void Log(String text) {
        Log.d(QuickSettingsToggle.class.getSimpleName(), text);
    }
}
