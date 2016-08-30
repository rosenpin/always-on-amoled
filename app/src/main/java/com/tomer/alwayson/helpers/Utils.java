package com.tomer.alwayson.helpers;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;
import com.tomer.alwayson.services.MainService;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Utils implements ContextConstatns {
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void showErrorNotification(Context context, String title, String text, int id, PendingIntent onClickIntent) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_error_outline);
        if (onClickIntent != null)
            builder.setContentIntent(onClickIntent);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setVibrate(new long[0]);
        Notification notification = builder.build();
        NotificationManager notificationManger = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(id, notification);
    }

    public static boolean stopMainService(Context context) {
        Utils.logDebug("Trying to stop, Main service is initialized", String.valueOf(MainService.initialized));
        if (MainService.initialized) {
            context.stopService(new Intent(context, MainService.class));
            return true;
        }
        return false;
    }

    public static void openURL(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
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

    public static void killBackgroundProcesses(Context context) {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = context.getPackageName();
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
            if (packageInfo.packageName.equals(myPackage)) continue;
            mActivityManager.killBackgroundProcesses(packageInfo.packageName);
        }
    }

    public static boolean hasModifySecurePermission(Context activity) {
        try {
            int originalBatteryMode = Settings.Secure.getInt(activity.getContentResolver(), LOW_POWER, 0);
            Settings.Secure.putInt(activity.getContentResolver(), LOW_POWER, 1);
            Settings.Secure.putInt(activity.getContentResolver(), LOW_POWER, originalBatteryMode);
            return true;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public static void logDebug(String var1, String var2) {
        if (var1 != null && var2 != null)
            Log.d(var1, var2);
    }

    public static void logError(String var1, String var2) {
        if (var1 != null && var2 != null)
            Log.e(var1, var2);
    }

    public static void logInfo(String var1, String var2) {
        if (var1 != null && var2 != null)
            Log.i(var1, var2);
    }

    public static boolean isAndroidNewerThanL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isAndroidNewerThanM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isAndroidNewerThanN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static class Animations {
        private static final int animLength = 300;
        private static final int actionDelay = animLength / 2;

        public static void fadeOutWithAction(View view, Runnable action) {
            view.animate().alpha(0).setDuration(animLength).setInterpolator(new FastOutSlowInInterpolator());
            new Handler().postDelayed(action, actionDelay);
        }

        public static void slideOutWithAction(View view, int finalY, Runnable action) {
            view.animate().translationY(finalY).alpha(0).setDuration(animLength).setInterpolator(new FastOutSlowInInterpolator());
            new Handler().postDelayed(action, actionDelay);
        }
    }
}
