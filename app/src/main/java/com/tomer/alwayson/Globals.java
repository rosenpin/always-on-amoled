package com.tomer.alwayson;

import android.graphics.drawable.Drawable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Globals {
    public static boolean isShown;
    public static boolean sensorIsScreenOff;
    public static boolean inCall = false;
    public static Map<String, Drawable> notificationsDrawables = new ConcurrentHashMap<>();
}
