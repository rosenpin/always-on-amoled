package com.tomer.alwayson.tasker.bundle;

import android.content.Intent;
import android.os.Bundle;

public final class BundleScrubber {
    public static boolean scrub(final Intent intent) {
        return null != intent && scrub(intent.getExtras());
    }

    public static boolean scrub(final Bundle bundle) {
        if (null == bundle) {
            return false;
        }
        try {
            bundle.containsKey(null);
        } catch (final Exception e) {
            bundle.clear();
            return true;
        }
        return false;
    }
}