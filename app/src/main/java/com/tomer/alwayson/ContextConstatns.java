package com.tomer.alwayson;

import android.service.notification.NotificationListenerService;

import com.tomer.alwayson.Activities.MainActivity;
import com.tomer.alwayson.Receivers.UnlockReceiver;
import com.tomer.alwayson.Services.MainService;
import com.tomer.alwayson.Services.WidgetUpdater;

/**
 * Created by tomer AKA rosenpin on 6/18/16.
 */
public interface ContextConstatns {
    //NAMES
    boolean ON = true;
    boolean OFF = false;

    float NIGHT_MODE_ALPHA = 0.3f;

    //TAGS
    String MAIN_SERVICE_LOG_TAG = MainService.class.getSimpleName();
    String WAKE_LOCK_TAG = "StayAwakeWakeLock";

    String NOTIFICATION_LISTENER_TAG = NotificationListenerService.class.getSimpleName();

    String MAINACTIVITY_TAG = MainActivity.class.getSimpleName();

    String UNLOCK_RECEIVER_TAG = UnlockReceiver.class.getSimpleName();
    String WIDGET_UPDATER_TAG = WidgetUpdater.class.getSimpleName();
}
