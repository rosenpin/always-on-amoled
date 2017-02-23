package com.tomer.alwayson;

import android.content.Intent;
import android.telephony.TelephonyManager;

public class Constants {
    public static final String[] unlockFilters = {
            //DEVICE UNLOCKED
            Intent.ACTION_USER_PRESENT,
            //ASSIST IE: GOOGLE NOW
            Intent.ACTION_ASSIST,
            //Calls
            TelephonyManager.ACTION_PHONE_STATE_CHANGED,
            AlarmClock.ACTION_SHOW_ALARMS,
            //ALARMS
            "com.android.deskclock.ALARM_ALERT",
            "com.android.alarmclock.ALARM_ALERT",
            "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT",
            "com.htc.android.worldclock.ALARM_ALERT",
            "com.sonyericsson.alarm.ALARM_ALERT",
            "zte.com.cn.alarmclock.ALARM_ALERT",
            "com.motorola.blur.alarmclock.ALARM_ALERT",
            "com.urbandroid.sleep.alarmclock.ALARM_ALERT",
            "com.lge.alarm.alarmclocknew",
            "com.sec.android.app.clockpackage.alarm.ALARM_ALERT",
            "com.samsung.sec.android.clockpackage.alarm.ALARM_STARTED_IN_ALERT",
            "com.sec.android.app.clockpackage.SHOW_ALARMS",
            "com.samsung.sec.android.clockpackage.START_CLOCKPACKAGE"
    };
    public static final String pluginPackageName = "com.tomer.alwaysonamoledplugin";
    public static final String oldPluginPackageName = "tomer.com.alwaysonamoledplugin";
}
