package com.tomer.alwayson;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    public boolean enabled;
    public boolean touchToStop;
    public boolean swipeToStop;
    public boolean volumeToStop;
    public boolean backButtonToStop;
    public boolean showNotification;
    public boolean moveWidget;
    public int brightness;
    public boolean disableVolumeKeys;
    public boolean notificationsAlerts;

    public boolean permissionGranting;

    private SharedPreferences prefs;

    public Prefs(Context context) {
        prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    }

    public void apply() {
        enabled = prefs.getBoolean(KEYS.ENABLED.toString(), true);
        touchToStop = prefs.getBoolean(KEYS.TOUCH_TO_STOP.toString(), false);
        swipeToStop = prefs.getBoolean(KEYS.SWIPE_TO_STOP.toString(), false);
        volumeToStop = prefs.getBoolean(KEYS.VOLUME_TO_STOP.toString(), true);
        backButtonToStop = prefs.getBoolean(KEYS.BACK_BUTTON_TO_STOP.toString(), false);
        showNotification = prefs.getBoolean(KEYS.SHOW_NOTIFICATION.toString(), true);
        moveWidget = prefs.getBoolean(KEYS.MOVE_WIDGET.toString(), true);
        notificationsAlerts = prefs.getBoolean(KEYS.NOTIFICATION_ALERTS.toString(), false);
        brightness = prefs.getInt(KEYS.BRIGHTNESS.toString(), 40);
        permissionGranting = prefs.getBoolean(KEYS.PERMISSION_GRANTING.toString(), false);
        disableVolumeKeys = prefs.getBoolean(KEYS.DISABLE_VOLUME_KEYS.toString(), true);
    }

    public void setString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public void setInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public void setBool(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public void forceBool(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
    }

    public enum KEYS {
        ENABLED("enabled"),
        TOUCH_TO_STOP("touchtostop"),
        SWIPE_TO_STOP("swipetostop"),
        VOLUME_TO_STOP("volumetostop"),
        BACK_BUTTON_TO_STOP("backbuttontostop"),
        SHOW_NOTIFICATION("shownotification"),
        MOVE_WIDGET("reduceamoledburnin"),
        BRIGHTNESS("brightness"),
        PERMISSION_GRANTING("permissiongrantingscreen"),
        DISABLE_VOLUME_KEYS("disablevolumekeys"),
        NOTIFICATION_ALERTS("notificationsalertsfromotherapps");

        private final String id;

        KEYS(final String text) {
            this.id = text;
        }

        @Override
        public String toString() {
            return id;
        }

    }
}

