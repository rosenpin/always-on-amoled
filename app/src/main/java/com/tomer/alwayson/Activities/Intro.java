package com.tomer.alwayson.Activities;

import android.Manifest;
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

/**
 * Created by tomer on 6/12/16.
 */
public class Intro extends AppIntro2 {

    Prefs pref;
    boolean[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new Prefs(getApplicationContext());
        pref.apply();
        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.

        addSlide(new First());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(new Second());
            addSlide(new Third());
            permissions = new boolean[3];
        } else
            permissions = new boolean[1];


        setNextPageSwipeLock(true);

        // Hide Skip/Done button.
        setProgressButtonEnabled(true);
        setVibrate(false);
        skipButtonEnabled = false;

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        for (int i = 0; i < permissions.length; i++) {
            if (!permissions[i]) {
                Snackbar.make(findViewById(android.R.id.content), "Please allow all permissions", Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        pref.setBool(Prefs.KEYS.PERMISSION_GRANTING.toString(), true);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    class First extends Fragment {
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
                View view = new View(getApplicationContext());
                ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            } catch (Exception e) {
                permissions[0] = false;
                go.setTextColor(getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
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
                View view = new View(getApplicationContext());
                ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(view, lp);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);
                Button go = (Button) v.findViewById(R.id.go);
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
                permissions[0] = true;
            } catch (Exception e) {
                permissions[0] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }

            return v;
        }
    }

    class Second extends Fragment {
        Button go;
        View v;

        @Override
        public void onResume() {
            super.onResume();
            if (!Settings.System.canWrite(getApplicationContext())) {
                permissions[1] = false;
                go.setTextColor(getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else {
                permissions[1] = true;
                Button go = (Button) v.findViewById(R.id.go);
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_second, container, false);
            go = (Button) v.findViewById(R.id.go);
            if (!Settings.System.canWrite(getApplicationContext())) {
                permissions[1] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else {
                permissions[1] = true;
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }

            return v;
        }
    }

    class Third extends Fragment {
        View v;
        Button go;

        @Override
        public void onResume() {
            super.onResume();
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                go.setTextColor(getColor(android.R.color.black));
                go.setText(getString(R.string.allow));
                go.setEnabled(true);
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                123);
                    }
                });
            } else {
                permissions[permissions.length - 1] = true;
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }

        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            v = inflater.inflate(R.layout.intro_third, container, false);
            go = (Button) v.findViewById(R.id.go);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions[permissions.length - 1] = false;
                v.findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                123);
                    }
                });
            } else {
                permissions[permissions.length - 1] = true;
                go.setTextColor(getColor(R.color.green));
                go.setText(getString(R.string.done_button));
                go.setEnabled(false);
            }
            return v;
        }
    }


}