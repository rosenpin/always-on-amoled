package com.tomer.alwayson;

import android.graphics.drawable.Drawable;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Globals {
    public static boolean isShown;
    public static boolean sensorIsScreenOff;
    public static boolean inCall = false;
    public static Map<String, Drawable> notificationsDrawables = new ConcurrentHashMap<>();
    public static boolean notificationChanged;
    public static ColorChooserDialog.Builder colorDialog;
    public static boolean noLock;
    public static boolean killedByDelay;
}
