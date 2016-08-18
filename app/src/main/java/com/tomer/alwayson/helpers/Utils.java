package com.tomer.alwayson.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import com.tomer.alwayson.R;
import com.tomer.alwayson.services.MainService;

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

    public static void showErrorNotification(Context context, String title, String text, int id, PendingIntent onClickIntent) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_error_outline);
        if (onClickIntent!=null)
            builder.setContentIntent(onClickIntent);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setVibrate(new long[0]);
        Notification notification = builder.build();
        NotificationManager notificationManger = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(id, notification);
    }

    public static boolean stopMainService(Context context) {
        Log.d("Trying to stop, Main service is initialized", String.valueOf(MainService.initialized));
        if (MainService.initialized) {
            context.stopService(new Intent(context, MainService.class));
            return true;
        }
        return false;
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
