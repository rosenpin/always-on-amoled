package com.heinrichreimersoftware.androidissuereporter.util;

import android.support.annotation.ColorInt;

public class ColorUtils {
    public static boolean isDark(@ColorInt int color) {
        return android.support.v4.graphics.ColorUtils.calculateLuminance(color) < .6;
    }
}
