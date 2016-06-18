package com.tomer.alwayson;

import android.graphics.drawable.Drawable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {
    public static boolean isShown;
    public static boolean sensorIsScreenOff;
    public static Map<String, Drawable> notificationsDrawables = new ConcurrentHashMap<>();
}
