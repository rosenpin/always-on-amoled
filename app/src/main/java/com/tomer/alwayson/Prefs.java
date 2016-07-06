package com.tomer.alwayson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

public class Prefs {
    public boolean enabled;
    public boolean doubleTapToStop, swipeToStop, volumeToStop, backButtonToStop;
    public boolean proximityToLock;
    public boolean showNotification;
    public boolean moveWidget;
    public boolean disableVolumeKeys;
    public boolean notificationsAlerts;
    public int clockStyle, dateStyle, batteryStyle;
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
    public int font;
    Context context;


    public Prefs(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public void apply() {
        enabled = prefs.getBoolean(KEYS.ENABLED.toString(), true);
        try {
            doubleTapToStop = prefs.getString(KEYS.DOUBLE_TAP_TO_STOP.toString(), "unlock").equals("unlock");
            swipeToStop = prefs.getString(KEYS.SWIPE_TO_STOP.toString(), "off").equals("unlock");
            volumeToStop = prefs.getString(KEYS.VOLUME_TO_STOP.toString(), "off").equals("unlock");
            backButtonToStop = prefs.getString(KEYS.BACK_BUTTON_TO_STOP.toString(), "off").equals("unlock");
        } catch (ClassCastException e) {
            prefs.edit().remove(KEYS.DOUBLE_TAP_TO_STOP.toString()).apply();
            prefs.edit().remove(KEYS.SWIPE_TO_STOP.toString()).apply();
            prefs.edit().remove(KEYS.VOLUME_TO_STOP.toString()).apply();
            prefs.edit().remove(KEYS.BACK_BUTTON_TO_STOP.toString()).apply();
            Toast.makeText(context, "ERROR, YOUR PREFERENCES WERE RESET", Toast.LENGTH_LONG).show();
        }
        showNotification = prefs.getBoolean(KEYS.SHOW_NOTIFICATION.toString(), true);
        moveWidget = prefs.getBoolean(KEYS.MOVE_WIDGET.toString(), true);
        notificationsAlerts = prefs.getBoolean(KEYS.NOTIFICATION_ALERTS.toString(), false);
        brightness = prefs.getInt(KEYS.BRIGHTNESS.toString(), 20);
        textSize = prefs.getInt(KEYS.TEXT_SIZE.toString(), 88);
        permissionGranting = prefs.getBoolean(KEYS.PERMISSION_GRANTING.toString(), false);
        disableVolumeKeys = prefs.getBoolean(KEYS.DISABLE_VOLUME_KEYS.toString(), true);
        proximityToLock = prefs.getBoolean(KEYS.PROXIMITY_TO_LOCK.toString(), false);
        clockStyle = Integer.parseInt(prefs.getString(KEYS.TIME_STYLE.toString(), "1"));
        dateStyle = Integer.parseInt(prefs.getString(KEYS.DATE_STYLE.toString(), "1"));
        batteryStyle = Integer.parseInt(prefs.getString(KEYS.BATTERY_STYLE.toString(), "0"));
        showAmPm = prefs.getBoolean(KEYS.SHOW_AM_PM.toString(), false);
        textColor = prefs.getInt(KEYS.TEXT_COLOR.toString(), -1);
        rules = prefs.getString(KEYS.RULES.toString(), "always");
        stopDelay = prefs.getString(KEYS.STOP_DELAY.toString(), "0");
        orientation = prefs.getString(KEYS.ORIENTATION.toString(), "vertical");
        stopOnCamera = prefs.getBoolean(KEYS.STOP_ON_CAMERA.toString(), true);
        batteryRules = Integer.parseInt(prefs.getString(KEYS.BATTERY_RULES.toString(), "0"));
        autoNightMode = prefs.getBoolean(KEYS.AUTO_NIGHT_MODE.toString(), false);
        font = Integer.parseInt(prefs.getString(KEYS.FONT.toString(), "0"));
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

    public void forceString(String key, String value) {
        prefs.edit().putString(key, value).commit();
    }

    public boolean getBoolByKey(String key, boolean b) {
        return prefs.getBoolean(key, b);
    }

    public void forceInt(String key, int value) {
        prefs.edit().putInt(key, value).commit();
    }

    public String getStringByKey(String key, String s) {
        return prefs.getString(key, s);
    }

    public enum KEYS {
        ENABLED("enabled"),
        DOUBLE_TAP_TO_STOP("double_tap"),
        SWIPE_TO_STOP("swipe_up"),
        VOLUME_TO_STOP("volume_keys"),
        BACK_BUTTON_TO_STOP("back_button"),
        SHOW_NOTIFICATION("persistent_notification"),
        MOVE_WIDGET("move_auto"),
        BRIGHTNESS("brightness"),
        TEXT_SIZE("font_size"),
        PERMISSION_GRANTING("permissiongrantingscreen"),
        DISABLE_VOLUME_KEYS("disable_volume_keys"),
        PROXIMITY_TO_LOCK("proximity_to_lock"),
        TIME_STYLE("watchface_clock"),
        DATE_STYLE("watchface_date"),
        BATTERY_STYLE("watchface_battery"),
        SHOW_AM_PM("showampm"),
        TEXT_COLOR("textcolor"),
        AUTO_NIGHT_MODE("auto_brightness"),
        RULES("rules"),
        STOP_DELAY("stop_delay"),
        HAS_SOFT_KEYS("has_soft_keys"),
        ORIENTATION("screen_orientation"),
        STOP_ON_CAMERA("stop_on_camera"),
        BATTERY_RULES("battery_rules"),
        NOTIFICATION_ALERTS("notifications_alerts"),
        FONT("font");

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

    public SharedPreferences getSharedPrefs() {
        return prefs;
    }
}

