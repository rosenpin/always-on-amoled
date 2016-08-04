package com.tomer.alwayson.Helpers;

import android.content.Context;
import android.content.pm.PackageManager;

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
}
