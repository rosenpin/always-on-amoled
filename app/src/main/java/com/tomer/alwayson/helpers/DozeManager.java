package com.tomer.alwayson.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.tomer.alwayson.ContextConstatns;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class DozeManager implements ContextConstatns {

    public DozeManager(Context context) {
        if (!isDumpPermissionGranted(context))
            grantPermission(context, "android.permission.DUMP");
        if (!isDevicePowerPermissionGranted(context))
            grantPermission(context, "android.permission.DEVICE_POWER");
        executeCommand("dumpsys deviceidle whitelist +" + context.getPackageName());
    }

    public static void executeCommand(final String command) {
        AsyncTask.execute(() -> {
            List<String> output = Shell.SH.run(command);
            if (output == null)
                Utils.logInfo(DOZE_MANAGER, "Error occurred while executing command (" + command + ")");
        });
    }

    public static boolean isDevicePowerPermissionGranted(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isDumpPermissionGranted(Context context) {
        return context.checkCallingOrSelfPermission(Manifest.permission.DUMP) == PackageManager.PERMISSION_GRANTED;
    }

    public static void grantPermission(Context context, String permission) {
        executeCommand("pm grant " + context.getPackageName() + " " + permission);
    }

    public void enterDoze() {
        if (!getDeviceIdleState().equals("IDLE")) {
            Utils.logInfo(DOZE_MANAGER, "Entering Doze");
            if (Utils.isAndroidNewerThanN()) {
                executeCommand("dumpsys deviceidle force-idle deep");
            } else {
                executeCommand("dumpsys deviceidle force-idle");
            }
        } else {
            Utils.logInfo(DOZE_MANAGER, "enterDoze() received but skipping because device is already Dozing");
        }
    }

    public void exitDoze() {
        if (Utils.isAndroidNewerThanN()) {
            executeCommand("dumpsys deviceidle unforce");
        } else {
            executeCommand("dumpsys deviceidle step");
        }
    }

    private String getDeviceIdleState() {
        String state = "";
        List<String> output = Shell.SH.run("dumpsys deviceidle");
        String outputString = TextUtils.join(", ", output);
        if (outputString.contains("mState=ACTIVE")) {
            state = "ACTIVE";
        } else if (outputString.contains("mState=INACTIVE")) {
            state = "INACTIVE";
        } else if (outputString.contains("mState=IDLE_PENDING")) {
            state = "IDLE_PENDING";
        } else if (outputString.contains("mState=SENSING")) {
            state = "SENSING";
        } else if (outputString.contains("mState=LOCATING")) {
            state = "LOCATING";
        } else if (outputString.contains("mState=IDLE")) {
            state = "IDLE";
        } else if (outputString.contains("mState=IDLE_MAINTENANCE")) {
            state = "IDLE_MAINTENANCE";
        }
        return state;
    }
}
