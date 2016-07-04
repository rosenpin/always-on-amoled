package com.tomer.alwayson;

import android.service.notification.NotificationListenerService;

import com.tomer.alwayson.Activities.PreferencesActivity;
import com.tomer.alwayson.Receivers.ChargeChangeReceiver;
import com.tomer.alwayson.Receivers.ScreenReceiver;
import com.tomer.alwayson.Receivers.UnlockReceiver;
import com.tomer.alwayson.Services.MainService;
import com.tomer.alwayson.Services.WidgetUpdater;

public interface ContextConstatns {
    //NAMES
    boolean ON = true;
    boolean OFF = false;

    float NIGHT_MODE_ALPHA = 0.3f;

    //TAGS
    String MAIN_SERVICE_LOG_TAG = MainService.class.getSimpleName();
    String MAIN_ACTIVITY_LOG_TAG = PreferencesActivity.class.getSimpleName();
    String WAKE_LOCK_TAG = "StayAwakeWakeLock";

    String NOTIFICATION_LISTENER_TAG = NotificationListenerService.class.getSimpleName();

    String UNLOCK_RECEIVER_TAG = UnlockReceiver.class.getSimpleName();
    String WIDGET_UPDATER_TAG = WidgetUpdater.class.getSimpleName();

    String CHARGER_RECEIVER_LOG_TAG = ChargeChangeReceiver.class.getSimpleName();
    String SCREEN_RECEIVER_LOG_TAG = ScreenReceiver.class.getSimpleName();

    int NOTIFICATION_LISTENER_REQUEST_CODE = 3;
    int DEVICE_ADMIN_REQUEST_CODE = 4;

    String DOUBLE_TAP = "double_tap";
    String SWIPE_UP = "swipe_up";
    String VOLUME_KEYS = "volume_keys";
    String BACK_BUTTON = "back_button";
}
