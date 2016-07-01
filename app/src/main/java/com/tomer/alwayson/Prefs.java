package com.tomer.alwayson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class Prefs {
    public boolean enabled;
    public boolean touchToStop, swipeToStop, volumeToStop, backButtonToStop;
    public boolean proximityToLock;
    public boolean showNotification;
    public boolean moveWidget;
    public boolean disableVolumeKeys;
    public boolean notificationsAlerts;
    public boolean showTime, showDate, showBattery;
    public boolean permissionGranting;
    public boolean showAmPm;
    public float textSize;
    public int textColor;
    public int brightness;
    public String stopDelay;
    public String rules;
    public String orientation;
    public boolean stopOnCamera;
    public boolean autoNightMode;
    public int batteryRules;
    private SharedPreferences prefs;


    public Prefs(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
        textSize = prefs.getInt(KEYS.TEXT_SIZE.toString(), 88);
        permissionGranting = prefs.getBoolean(KEYS.PERMISSION_GRANTING.toString(), false);
        disableVolumeKeys = prefs.getBoolean(KEYS.DISABLE_VOLUME_KEYS.toString(), true);
        proximityToLock = prefs.getBoolean(KEYS.PROXIMITY_TO_LOCK.toString(), false);
        showTime = prefs.getBoolean(KEYS.SHOW_TIME.toString(), true);
        showDate = prefs.getBoolean(KEYS.SHOW_DATE.toString(), true);
        showBattery = prefs.getBoolean(KEYS.SHOW_BATTERY.toString(), false);
        showAmPm = prefs.getBoolean(KEYS.SHOW_AM_PM.toString(), false);
        textColor = prefs.getInt(KEYS.TEXT_COLOR.toString(), -1);
        rules = prefs.getString(KEYS.RULES.toString(), "always");
        stopDelay = prefs.getString(KEYS.STOP_DELAY.toString(), "0");
        orientation = prefs.getString(KEYS.ORIENTATION.toString(), "vertical");
        stopOnCamera = prefs.getBoolean(KEYS.STOP_ON_CAMERA.toString(), true);
        batteryRules = Integer.parseInt(prefs.getString(KEYS.BATTERY_RULES.toString(), "11"));
        autoNightMode = prefs.getBoolean(KEYS.AUTO_NIGHT_MODE.toString(), false);
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

    public boolean getBoolByKey(String key, boolean b) {
        return prefs.getBoolean(key, b);
    }

    public void forceInt(String key, int value) {
        prefs.edit().putInt(key, value).commit();
    }

    public enum KEYS {
        ENABLED("enabled"),
        TOUCH_TO_STOP("double_tap_dismiss"),
        SWIPE_TO_STOP("swipe_dismiss"),
        VOLUME_TO_STOP("volume_dismiss"),
        BACK_BUTTON_TO_STOP("back_button_dismiss"),
        SHOW_NOTIFICATION("persistent_notification"),
        MOVE_WIDGET("move_auto"),
        BRIGHTNESS("brightness"),
        TEXT_SIZE("font_size"),
        PERMISSION_GRANTING("permissiongrantingscreen"),
        DISABLE_VOLUME_KEYS("disable_volume_keys"),
        PROXIMITY_TO_LOCK("proximity_to_lock"),
        SHOW_TIME("show_time"),
        SHOW_BATTERY("show_battery"),
        SHOW_DATE("show_date"),
        SHOW_AM_PM("showampm"),
        TEXT_COLOR("textcolor"),
        AUTO_NIGHT_MODE("auto_brightness"),
        RULES("rules"),
        STOP_DELAY("stop_delay"),
        HAS_SOFT_KEYS("has_soft_keys"),
        ORIENTATION("screen_orientation"),
        STOP_ON_CAMERA("stop_on_camera"),
        BATTERY_RULES("battery_rules"),
        NOTIFICATION_ALERTS("notifications_alerts");

        private final String id;

        KEYS(final String text) {
            this.id = text;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public String toString() {
        Map<String, ?> keys = prefs.getAll();
        StringBuilder string = new StringBuilder();
        string.append("Prefs");
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            string.append(entry.getKey() + ": " +
                    entry.getValue().toString() + "\n");
        }
        return string.toString();
    }
}

