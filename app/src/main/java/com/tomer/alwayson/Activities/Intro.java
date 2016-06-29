package com.tomer.alwayson.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.github.paolorotolo.appintro.AppIntro2;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;


public class Intro extends AppIntro2 {
    static Context context;
    static Prefs pref;
    static boolean[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        pref = new Prefs(getApplicationContext());
        pref.apply();

        addSlide(new First());
        addSlide(new Second());
        addSlide(new Third());
        permissions = new boolean[3];


        setNextPageSwipeLock(true);

        // Hide Skip/Done button.
        setProgressButtonEnabled(true);
        setVibrate(false);
        skipButtonEnabled = false;

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        for (boolean permission : permissions) {
            if (!permission) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.please_allow_all_permissions), Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        pref.setBool(Prefs.KEYS.PERMISSION_GRANTING.toString(), true);
        startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    public static class First extends Fragment {
        Button go;
        View v;

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
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            } catch (Exception e) {
                permissions[0] = false;
                go.setTextColor(context.getResources().getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_first, container, false);
            try {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2003, 65794, -2);
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                View view = new View(context);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) context.getSystemService(WINDOW_SERVICE)).removeView(view);
                Button go = (Button) v.findViewById(R.id.go);
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
                permissions[0] = true;
            } catch (Exception e) {
                permissions[0] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }

            return v;
        }
    }

    public static class Second extends Fragment {
        Button go;
        View v;

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onResume() {
            super.onResume();
            if (!Settings.System.canWrite(context)) {
                permissions[1] = false;
                go.setTextColor(context.getResources().getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else {
                permissions[1] = true;
                Button go = (Button) v.findViewById(R.id.go);
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_second, container, false);
            go = (Button) v.findViewById(R.id.go);
            if (!Settings.System.canWrite(context)) {
                permissions[1] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else {
                permissions[1] = true;
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }

            return v;
        }
    }

    public static class Third extends Fragment {
        View v;
        Button go;

        @Override
        public void onResume() {
            super.onResume();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                go.setTextColor(context.getResources().getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.CAMERA},
                                123);
                    }
                });
            } else {
                permissions[permissions.length - 1] = true;
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }

        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_third, container, false);
            go = (Button) v.findViewById(R.id.go);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA},
                                123);
                    }
                });
            } else {
                permissions[permissions.length - 1] = true;
                go.setTextColor(context.getResources().getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }
            return v;
        }
    }
}
