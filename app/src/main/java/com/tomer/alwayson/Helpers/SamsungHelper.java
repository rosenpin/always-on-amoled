package com.tomer.alwayson.Helpers;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;

import java.io.IOException;

public class SamsungHelper implements ContextConstatns {
    private Prefs prefs;
    private int originalCapacitiveButtonsState;
    private ContentResolver contentResolver;
    private Context context;

    public SamsungHelper(Context context, Prefs prefs) {
        this.prefs = prefs;
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public int getButtonsLight() {
        if (!prefs.hasSoftKeys) {
            try {
                originalCapacitiveButtonsState = Settings.System.getInt(contentResolver, "button_key_light");
            } catch (Settings.SettingNotFoundException e) {
                Log.d(MAIN_SERVICE_LOG_TAG, "First method of getting the buttons status failed.");
                try {
                    originalCapacitiveButtonsState = (int) Settings.System.getLong(contentResolver, "button_key_light");
                } catch (Exception ignored) {
                    Log.d(MAIN_SERVICE_LOG_TAG, "Second method of getting the buttons status failed.");
                    try {
                        originalCapacitiveButtonsState = Settings.Secure.getInt(contentResolver, "button_key_light");
                    } catch (Exception ignored3) {
                        Log.d(MAIN_SERVICE_LOG_TAG, "Third method of getting the buttons status failed.");
                    }
                }
            }
        }
        return originalCapacitiveButtonsState;
    }

    public void setButtonsLight(boolean state) {
        state = !state;
        if (!prefs.hasSoftKeys) {
            if (Utils.isPackageInstalled(context, "tomer.com.alwaysonamoledplugin")) {
                try {
                    Intent i = new Intent();
                    i.setComponent(new ComponentName("tomer.com.alwaysonamoledplugin", "tomer.com.alwaysonamoledplugin.CapacitiveButtons"));
                    i.putExtra("state", state);
                    i.putExtra("originalCapacitiveButtonsState", originalCapacitiveButtonsState);
                    ComponentName c = context.startService(i);
                    Log.d(MAIN_SERVICE_LOG_TAG, "Started plugin to control the buttons lights");
                } catch (Exception e1) {
                    Log.d(MAIN_SERVICE_LOG_TAG, "Fifth (plugin) method of settings the buttons state failed.");
                    Toast.makeText(context, context.getString(R.string.error_2_plugin_not_installed), Toast.LENGTH_LONG).show();
                    try {
                        Settings.System.putInt(contentResolver, "button_key_light", state ? 0 : originalCapacitiveButtonsState);
                    } catch (RuntimeException e2) {
                        Log.d(MAIN_SERVICE_LOG_TAG, "First method of settings the buttons state failed.");
                        try {
                            Runtime r = Runtime.getRuntime();
                            r.exec("echo" + (state ? 0 : originalCapacitiveButtonsState) + "> /system/class/leds/keyboard-backlight/brightness");
                        } catch (IOException e3) {
                            Log.d(MAIN_SERVICE_LOG_TAG, "Second method of settings the buttons state failed.");
                            try {
                                Settings.System.putLong(contentResolver, "button_key_light", state ? 0 : originalCapacitiveButtonsState);
                            } catch (Exception e4) {
                                Log.d(MAIN_SERVICE_LOG_TAG, "Third method of settings the buttons state failed.");
                                try {
                                    Settings.Secure.putInt(contentResolver, "button_key_light", state ? 0 : originalCapacitiveButtonsState);
                                } catch (Exception e5) {
                                    Log.d(MAIN_SERVICE_LOG_TAG, "Fourth method of settings the buttons state failed.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private HomeWatcher homeWatcher;

    public void setOnHomeButtonClickListener(final Runnable action) {
        homeWatcher = new HomeWatcher(context);
        homeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                action.run();
            }
        });
        homeWatcher.startWatch();
    }

    public void stopHomeWatcher() {
        homeWatcher.stopWatch();
    }
}
