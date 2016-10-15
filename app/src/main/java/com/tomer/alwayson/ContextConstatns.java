package com.tomer.alwayson;

import android.service.notification.NotificationListenerService;

import com.tomer.alwayson.activities.PreferencesActivity;
import com.tomer.alwayson.helpers.DozeManager;
import com.tomer.alwayson.helpers.GreenifyStarter;
import com.tomer.alwayson.receivers.BootReceiver;
import com.tomer.alwayson.receivers.ChargeChangeReceiver;
import com.tomer.alwayson.receivers.ScreenReceiver;
import com.tomer.alwayson.receivers.UnlockReceiver;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.services.WidgetUpdater;

public interface ContextConstatns {
    //NAMES
    boolean ON = true;
    boolean OFF = false;

    float NIGHT_MODE_ALPHA = 0.3f;

    //Intent filters
    String FINISH_HOME_BUTTON_ACTIVITY = "samsung_home_button_activity_finish_self";
    String NEW_NOTIFICATION = "new_notification";
    String TOGGLED = "service toggled";
    String NOTIFICATIONS = "notifications";
    String LAST_NOTIFICATION = "last_notification";

    //TAGS
    String WAKE_LOCK_TAG = "StayAwakeWakeLock";
    String MAIN_SERVICE_LOG_TAG = MainService.class.getSimpleName();
    String MAIN_ACTIVITY_LOG_TAG = PreferencesActivity.class.getSimpleName();
    String GREENIFY_STARTER = GreenifyStarter.class.getSimpleName();
    String NOTIFICATION_LISTENER_TAG = NotificationListenerService.class.getSimpleName();
    String UNLOCK_RECEIVER_TAG = UnlockReceiver.class.getSimpleName();
    String WIDGET_UPDATER_TAG = WidgetUpdater.class.getSimpleName();
    String CHARGER_RECEIVER_LOG_TAG = ChargeChangeReceiver.class.getSimpleName();
    String SCREEN_RECEIVER_LOG_TAG = ScreenReceiver.class.getSimpleName();
    String BOOT_RECEIVER = BootReceiver.class.getSimpleName();
    String DOZE_MANAGER = DozeManager.class.getSimpleName();

    String LOW_POWER = "low_power";

    int NOTIFICATION_LISTENER_REQUEST_CODE = 3;
    int CAMERA_PERMISSION_REQUEST_CODE = 5;
    int DEVICE_ADMIN_REQUEST_CODE = 4;
    int RESULT_BILLING_UNAVAILABLE = 3;

    int reportNotificationID = 53;

    String DOUBLE_TAP = "double_tap_action";
    String SWIPE_UP = "swipe_up_action";
    String SWIPE_DOWN = "swipe_down_action";
    String VOLUME_KEYS = "volume_keys_action";
    String BACK_BUTTON = "back_button_action";

    int ACTION_OFF_GESTURE = 0;
    int ACTION_UNLOCK = 1;
    int ACTION_SPEAK = 2;
    int ACTION_FLASHLIGHT = 3;

    String HORIZONTAL = "horizontal";
    String VERTICAL = "vertical";

    int PROXIMITY_NORMAL_MODE = 3;
    int PROXIMITY_DEVICE_ADMIN_MODE = 2;

    String GRID_TYPE = "grid_type";
    int GRID_TYPE_CLOCK = 1;
    int GRID_TYPE_DATE = 2;

    int DATE_TEXT = 1;
    int DATE_VIEW = 2;

    int DISABLED = 0;
    int MOVE_NO_ANIMATION = 1;
    int MOVE_WITH_ANIMATION = 2;

    int FADE_OUT = 1;
    int SLIDE_OUT = 2;

    int DIGITAL_CLOCK = 1;
    int ANALOG_CLOCK = 2;
    int S7_DIGITAL = 3;
    int ANALOG24_CLOCK = 4;
    int S7_CLOCK = 5;
    int PEBBLE_CLOCK = 6;
    int FLAT_CLOCK = 7;
    int FLAT_RED_CLOCK = 8;
    int FLAT_STANDARD_TICKS = 9;
}
