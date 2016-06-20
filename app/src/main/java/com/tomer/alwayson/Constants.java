package com.tomer.alwayson;

import android.content.Intent;

/**
 * Created by tomer AKA rosenpin on 6/18/16.
 */
public class Constants {
    public static final String[] unlockFilters = {
            //DEVICE UNLOCKED
            Intent.ACTION_USER_PRESENT,
            //ASSIST IE: GOOGLE NOW
            Intent.ACTION_ASSIST,
            Intent.ACTION_ALL_APPS,
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
