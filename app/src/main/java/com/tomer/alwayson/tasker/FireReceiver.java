package com.tomer.alwayson.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.tasker.bundle.BundleScrubber;
import com.tomer.alwayson.tasker.bundle.PluginBundleManager;

public final class FireReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        BundleScrubber.scrub(intent);
        Bundle bundle = intent.getBundleExtra(EditActivity.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);
        String mode = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
        assert mode != null;
        if (mode.equals("Start always on"))
            context.startService(new Intent(context, MainService.class));
        Log.d("Mode is", mode);
    }
}
