package com.tomer.alwayson.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;

import java.util.List;


public class Intro extends AppIntro2 {
    private static Context context;
    private static Prefs pref;
    private static boolean[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        pref = new Prefs(getApplicationContext());
        pref.apply();

        if (Utils.isAndroidNewerThanM()) {
            boolean isPhone;
            isPhone = Utils.isPhone(context);
            permissions = new boolean[isPhone ? 3 : 2];
            skipButtonEnabled = false;
            addSlide(new First());
            addSlide(new Second());
            if (isPhone)
                addSlide(new Third());
        } else {
            skipButtonEnabled = true;
        }

        addSlide(new Fourth());
        addSlide(new Fifth());

        setNextPageSwipeLock(true);
        setProgressButtonEnabled(true);
        setVibrate(false);
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (permissions != null) {
            for (boolean permission : permissions) {
                if (!permission) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.warning_8_intro_allow_all), Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
        }
        pref.setBool(Prefs.KEYS.PERMISSION_GRANTING.toString(), true);
        startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context = null;
        pref = null;
        permissions = null;
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    public static class First extends Fragment {
        private Button go;
        private View v;

        @Override
        public void onResume() {
            super.onResume();
            try {
                permissions[0] = true;
                go = (Button) v.findViewById(R.id.go);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                View view = new View(context);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).removeView(view);
                go.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                permissions[0] = false;
                go.setTextColor(context.getResources().getColor(android.R.color.black));
                go.setText(getString(R.string.intro_allow_now));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(v1 -> {
                    if (Utils.isAndroidNewerThanM()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_screen, container, false);
            v.findViewById(R.id.background).setBackgroundColor(Color.parseColor("#039be5"));
            ((TextView) v.findViewById(R.id.title)).setText(R.string.intro_draw_over);
            ((TextView) v.findViewById(R.id.description)).setText(R.string.intro_draw_over_desc);
            try {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                View view = new View(context);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).removeView(view);
                Button go = (Button) v.findViewById(R.id.go);
                go.setVisibility(View.INVISIBLE);
                permissions[0] = true;
            } catch (Exception e) {
                permissions[0] = false;
                v.findViewById(R.id.go).setOnClickListener(v1 -> {
                    if (Utils.isAndroidNewerThanM()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }

            return v;
        }
    }

    public static class Second extends Fragment {
        private Button go;
        private View v;

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onResume() {
            super.onResume();
            if (!Settings.System.canWrite(context)) {
                permissions[1] = false;
                go.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                go.setText(getString(R.string.intro_allow_now));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(v1 -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            } else {
                permissions[1] = true;
                Button go = (Button) v.findViewById(R.id.go);
                go.setVisibility(View.INVISIBLE);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_screen, container, false);
            v.findViewById(R.id.background).setBackgroundColor(Color.parseColor("#795548"));
            ((TextView) v.findViewById(R.id.title)).setText(R.string.intro_modify);
            ((TextView) v.findViewById(R.id.description)).setText(R.string.intro_modify_desc);
            go = (Button) v.findViewById(R.id.go);
            if (!Settings.System.canWrite(context)) {
                permissions[1] = false;
                v.findViewById(R.id.go).setOnClickListener(v1 -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
            } else {
                permissions[1] = true;
                go.setVisibility(View.INVISIBLE);
            }

            return v;
        }
    }

    public static class Third extends Fragment {
        private View v;
        private Button go;

        @Override
        public void onResume() {
            super.onResume();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                go.setTextColor(context.getResources().getColor(android.R.color.black));
                go.setText(getString(R.string.intro_allow_now));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(v1 -> ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        123));
            } else {
                permissions[permissions.length - 1] = true;
                go.setVisibility(View.INVISIBLE);
            }
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_screen, container, false);
            v.findViewById(R.id.background).setBackgroundColor(ContextCompat.getColor(context, R.color.intro_background_3));
            ((TextView) v.findViewById(R.id.title)).setText(R.string.intro_permissions);
            ((TextView) v.findViewById(R.id.description)).setText(R.string.intro_permissions_desc);
            go = (Button) v.findViewById(R.id.go);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                v.findViewById(R.id.go).setOnClickListener(v1 -> ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        123));
            } else {
                permissions[permissions.length - 1] = true;
                go.setVisibility(View.INVISIBLE);
            }
            return v;
        }
    }

    public static class Fourth extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.intro_fourth, container, false);
            pref.apply();
            CheckBox[] checkBoxes = {(CheckBox) v.findViewById(R.id.intro_checkbox_double_tap), (CheckBox) v.findViewById(R.id.intro_checkbox_swipe_up), (CheckBox) v.findViewById(R.id.intro_checkbox_volume_keys)};
            for (int i = 0; i < checkBoxes.length; i++) {
                final int finalI = i;
                checkBoxes[i].setOnCheckedChangeListener((compoundButton, b) -> {
                    switch (finalI) {
                        case 0:
                            pref.setString(Prefs.KEYS.DOUBLE_TAP_TO_STOP.toString(), b ? "unlock" : "off");
                            break;
                        case 1:
                            pref.setString(Prefs.KEYS.SWIPE_UP_ACTION.toString(), b ? "unlock" : "off");
                            break;
                        case 2:
                            pref.setString(Prefs.KEYS.VOLUME_TO_STOP.toString(), b ? "unlock" : "off");
                            break;
                    }
                });
            }
            return v;
        }
    }

    public static class Fifth extends Fragment {
        private View v;
        private boolean shouldEnableNotificationsAlerts;
        private Button go;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_screen, container, false);
            v.findViewById(R.id.background).setBackgroundColor(Color.parseColor("#9c27b0"));
            ((TextView) v.findViewById(R.id.title)).setText(R.string.intro_notifications);
            ((TextView) v.findViewById(R.id.description)).setText(R.string.intro_notifications_desc);
            go = (Button) v.findViewById(R.id.go);
            go.setOnClickListener(view -> {
                if (checkNotificationsPermission(getContext(), true)) {
                    go.setVisibility(View.INVISIBLE);
                    pref.forceBool(Prefs.KEYS.NOTIFICATION_ALERTS.toString(), true);
                }
            });
            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (shouldEnableNotificationsAlerts && checkNotificationsPermission(getContext(), false)) {
                go.setVisibility(View.INVISIBLE);
                pref.forceBool(Prefs.KEYS.NOTIFICATION_ALERTS.toString(), true);
            }
        }

        private boolean checkNotificationsPermission(Context c, boolean prompt) {
            ContentResolver contentResolver = c.getContentResolver();
            String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = c.getPackageName();

            if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                if (Utils.isAndroidNewerThanL() && prompt) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    shouldEnableNotificationsAlerts = true;
                } else if (prompt) {
                    checkAndStartActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    shouldEnableNotificationsAlerts = true;
                }
                Toast.makeText(getContext(), R.string.warning_9_allow_notification, Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        private void checkAndStartActivity(Intent intent) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0)
                startActivity(intent);
        }
    }
}
