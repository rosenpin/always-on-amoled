package com.heinrichreimersoftware.androidissuereporter.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleableRes;
import android.util.TypedValue;

import com.heinrichreimersoftware.androidissuereporter.R;

public class ThemeUtils {
    @ColorInt
    private static int[] resolveThemeColors(@NonNull Context context, @AttrRes @StyleableRes int[] attrs, @ColorInt int[] defaultColors) {
        if (attrs.length != defaultColors.length)
            throw new IllegalArgumentException("Argument attrs must be the same size as defaultColors");
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, attrs);

        for (int i = 0; i < attrs.length; i++) {
            defaultColors[i] = a.getColor(0, defaultColors[i]);
        }

        a.recycle();

        return defaultColors;
    }

    @ColorInt
    private static int resolveThemeColor(@NonNull Context context, @AttrRes @StyleableRes int attr) {
        return resolveThemeColors(context, new int[]{attr}, new int[]{0})[0];
    }

    @ColorInt
    public static int getColorAccent(@NonNull Context context) {
        return resolveThemeColor(context, R.attr.colorAccent);
    }
}
