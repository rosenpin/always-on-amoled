package com.tomer.alwayson.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.tasker.bundle.BundleScrubber;
import com.tomer.alwayson.tasker.bundle.PluginBundleManager;

public final class FireReceiver extends BroadcastReceiver {
    int errorNotificationID = 51;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Utils.logDebug(FireReceiver.class.getSimpleName(), "received");
        BundleScrubber.scrub(intent);
        Bundle bundle = intent.getBundleExtra(EditActivity.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);
        String mode = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
        assert mode != null;
        if (mode.equals("Start always on"))
            context.startService(new Intent(context, MainService.class));
        else if (mode.equals("Stop always on")) {
            if (MainService.initialized)
                Utils.stopMainService(context);
            else
                Utils.showErrorNotification(context, context.getString(R.string.error), context.getString(R.string.error_4_tasker_service_not_initialized), errorNotificationID, null);
        }
        Utils.logDebug("Mode is", mode);
    }
}
