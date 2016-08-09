package com.tomer.alwayson.Helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Random;

public class Utils {
    public static boolean isPackageInstalled(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static double randInt(double min, double max) {
        return new Random().nextInt((int) ((max - min) + 1)) + min;
    }

    public static String getDateText(Context context) {
        return DateUtils.formatDateTime(context, Calendar.getInstance().getTime().getTime(),
                DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_NO_YEAR
                        | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_ABBREV_MONTH
                        | DateUtils.FORMAT_ABBREV_WEEKDAY);
    }

    public static boolean isAndroidNewerThanL(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
    public static boolean isAndroidNewerThanM(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    public static boolean isAndroidNewerThanN(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
