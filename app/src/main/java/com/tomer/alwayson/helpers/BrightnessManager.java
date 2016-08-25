package com.tomer.alwayson.helpers;

import android.content.Context;
import android.provider.Settings;

public class BrightnessManager {
    private Context context;
    private int originalBrightness = 100;
    private int originalAutoBrightnessStatus;

    public BrightnessManager(Context context) {
        this.context = context;
        originalAutoBrightnessStatus = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        originalBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
    }

    public void setBrightness(int brightness, int brightnessMode) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
    }

    public int getOriginalBrightness() {
        return originalBrightness;
    }

    public int getOriginalBrightnessMode() {
        return originalAutoBrightnessStatus;
    }
}

