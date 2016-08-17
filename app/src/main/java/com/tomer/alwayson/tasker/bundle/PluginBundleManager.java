package com.tomer.alwayson.tasker.bundle;

import android.os.Bundle;
import android.text.TextUtils;

public final class PluginBundleManager {

    public static final String BUNDLE_EXTRA_STRING_MESSAGE = "com.yourcompany.yourapp.extra.STRING_MESSAGE"; //$NON-NLS-1$

    public static final String BUNDLE_EXTRA_INT_VERSION_CODE =
            "com.yourcompany.yourcondition.extra.INT_VERSION_CODE";

    public static boolean isBundleValid(final Bundle bundle) {
        return null != bundle && bundle.containsKey(BUNDLE_EXTRA_INT_VERSION_CODE) && 2 == bundle.keySet().size() && !TextUtils.isEmpty(bundle.getString(BUNDLE_EXTRA_STRING_MESSAGE)) && bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 0) == bundle.getInt(BUNDLE_EXTRA_INT_VERSION_CODE, 1);
    }

    public static Bundle generateBundle(String message) {
        final Bundle result = new Bundle();
        result.putString(BUNDLE_EXTRA_STRING_MESSAGE, message);
        return result;
    }

    private PluginBundleManager() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}