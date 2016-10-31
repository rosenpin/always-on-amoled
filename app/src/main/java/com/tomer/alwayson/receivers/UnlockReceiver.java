package com.tomer.alwayson.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.MainService;

public class UnlockReceiver extends BroadcastReceiver implements ContextConstatns {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT) && Globals.noLock) {
            return;
        }
        Utils.logInfo(UNLOCK_RECEIVER_TAG, "Received");
        MainService.stoppedByShortcut = true;
        Utils.stopMainService(context);
        Globals.isShown = false;
    }
}
