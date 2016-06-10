package com.tomer.alwayson;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by tomer aka rosenpin on 2/9/16.
 */
public class Prefs {
    public boolean enabled;
    public boolean touchToStop;
    public boolean showNotification;
    SharedPreferences prefs;
    Context context;
    public boolean swipeToStop;

    public Prefs(Context context) {
        prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        this.context = context;
    }

    public void apply() {
        enabled = prefs.getBoolean(KEYS.ENABLED.toString(), false);
        touchToStop = prefs.getBoolean(KEYS.TOUCH_TO_STOP.toString(), false);
        swipeToStop = prefs.getBoolean(KEYS.SWIPE_TO_STOP.toString(), false);
        showNotification = prefs.getBoolean(KEYS.SHOW_NOTIFICATION.toString(), false);
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
        SHOW_NOTIFICATION("shownotification");

        private final String id;

        private KEYS(final String text) {
            this.id = text;
        }

        @Override
        public String toString() {
            return id;
        }

    }
}

