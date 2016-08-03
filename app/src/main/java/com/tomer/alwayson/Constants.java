package com.tomer.alwayson;

import android.content.Intent;

public class Constants {
    public static final String[] unlockFilters = {
            //DEVICE UNLOCKED
            Intent.ACTION_USER_PRESENT,
            //ASSIST IE: GOOGLE NOW
            Intent.ACTION_ASSIST,
            //ALARMS
            "com.android.deskclock.ALARM_ALERT",
            "com.android.alarmclock.ALARM_ALERT",
            "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT",
            "com.htc.android.worldclock.ALARM_ALERT",
            "com.sonyericsson.alarm.ALARM_ALERT",
            "zte.com.cn.alarmclock.ALARM_ALERT",
            "com.motorola.blur.alarmclock.ALARM_ALERT",
            "com.urbandroid.sleep.alarmclock.ALARM_ALERT",
            "com.lge.alarm.alarmclocknew"};
}
