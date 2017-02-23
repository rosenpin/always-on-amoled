package com.tomer.alwayson.helpers;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;

import com.tomer.alwayson.Globals;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CurrentAppResolver {

    public static int GOOGLE_NOW = 301;
    public static int CAMERA = 302;

    private Context context;
    private ActivityManager activityManager;
    private Handler handler;
    private boolean active;
    private boolean firstLaunch = true;
    private ArrayList<String> appsPNs;

    public CurrentAppResolver(Context context, int[] apps) {
        this.context = context;
        this.active = true;
        this.handler = new Handler();
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (apps.length > 0) {
            appsPNs = new ArrayList<>();
            for (int app : apps) {
                if (app == GOOGLE_NOW)
                    appsPNs.add("com.google.android.googlequicksearchbox");
                if (app == CAMERA)
                    appsPNs.addAll(getCameraPackageName());
            }
            Utils.logDebug("Apps to stop for", appsPNs.toString());
            Utils.logInfo(CurrentAppResolver.class.getSimpleName(), "Started");
        }
    }

    private ArrayList<String> getCameraPackageName() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = context.getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<String> PNs = new ArrayList<>();
        if (listCam == null || listCam.isEmpty()) return null;
        for (ResolveInfo res : listCam) {
            PNs.add(res.activityInfo.packageName);
        }
        return PNs;
    }

    private String getActivePackagesCompat() {
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName componentName = taskInfo.get(0).topActivity;
        return componentName.getPackageName();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getActivePackages() {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 200 * 200, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            return mySortedMap.get(mySortedMap.lastKey()).getPackageName();
        }
        return context.getPackageName();
    }

    public void executeForCurrentApp(final Runnable action) {
        if (!appsPNs.isEmpty()) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.scheduleWithFixedDelay(() -> {
                if (active) {
                    final String activePackage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getActivePackages() : getActivePackagesCompat();
                    if (activePackage != null) {
                        for (final String appPackageName : appsPNs) {
                            if (activePackage.equals(appPackageName)) {
                                if (!firstLaunch)
                                    action.run();
                            }
                            if (firstLaunch) {
                                if (activePackage.equals(appPackageName)) {
                                    Utils.logError(CurrentAppResolver.class.getSimpleName(), "App was already open when service started.");
                                    Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                                handler.postDelayed(() -> {
                                    if (activePackage.equals(appPackageName)) {
                                        Utils.logError(CurrentAppResolver.class.getSimpleName(), "App couldn't be closed, stopping the listener");
                                        appsPNs.remove(appPackageName);
                                    }
                                    firstLaunch = false;
                                }, 1000);
                            }
                        }
                    }
                } else {
                    Globals.waitingForApp = false;
                    executor.shutdown();
                }
            }, 0L, 300, TimeUnit.MILLISECONDS);
        }
    }

    public void destroy() {
        active = false;
    }
}
