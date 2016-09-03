package com.tomer.alwayson;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.tomer.alwayson.services.NotificationListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Globals {
    public static boolean isShown;
    public static boolean sensorIsScreenOff;
    public static boolean inCall = false;
    public static boolean noLock;
    public static boolean killedByDelay;
    public static boolean isServiceRunning;
    public static boolean waitingForApp = false;
    public static boolean notificationChanged;
    public static Map<String, NotificationListener.NotificationHolder> notifications = new ConcurrentHashMap<>();
    public static ColorChooserDialog.Builder colorDialog;
    public static ArrayList<String> ownedItems;
    public static NotificationListener.NotificationHolder newNotification;
    public static Runnable onNotificationAction;
}
