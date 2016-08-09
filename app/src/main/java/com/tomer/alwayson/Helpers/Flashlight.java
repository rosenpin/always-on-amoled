package com.tomer.alwayson.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;

public class Flashlight {
    private boolean enabled;
    private Camera cam;
    private boolean isLoading;

    public Flashlight() {
        cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        SurfaceTexture mPreviewTexture = new SurfaceTexture(0);
        try {
            cam.setPreviewTexture(mPreviewTexture);
        } catch (IOException ignored) {
        }
    }

    public void toggle(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isLoading = true;
            if (!enabled) {
                cam.startPreview();
            } else {
                cam.stopPreview();
            }
            enabled = !enabled;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isLoading = false;
                }
            }, 500);
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void destroy() {
        cam.release();
    }
}
