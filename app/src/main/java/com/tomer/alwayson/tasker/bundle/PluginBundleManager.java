package com.tomer.alwayson.tasker.bundle;

import android.os.Bundle;

public final class PluginBundleManager {

    public static final String BUNDLE_EXTRA_STRING_MESSAGE = "com.yourcompany.yourapp.extra.STRING_MESSAGE";

    private PluginBundleManager() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    public static Bundle generateBundle(String message) {
        final Bundle result = new Bundle();
        result.putString(BUNDLE_EXTRA_STRING_MESSAGE, message);
        return result;
    }
}