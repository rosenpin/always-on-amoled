package com.tomer.alwayson.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.tomer.alwayson.R;

import java.io.IOException;

public class Flashlight {
    private boolean enabled;
    private Camera cam;
    private boolean isLoading;
    private Context context;

    public Flashlight(Context context) {
        this.context = context;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                SurfaceTexture mPreviewTexture = new SurfaceTexture(0);
                try {
                    cam.setPreviewTexture(mPreviewTexture);
                } catch (IOException ignored) {
                }
            } catch (RuntimeException e) {
                Utils.showErrorNotification(context, context.getString(R.string.error), context.getString(R.string.error_5_camera_cant_connect_desc), 233, null);
            }
        }
    }

    public void toggle() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cam != null) {
                isLoading = true;
                if (!enabled) {
                    try {
                        cam.startPreview();
                    } catch (RuntimeException e) {
                        Utils.showErrorNotification(context, context.getString(R.string.error), context.getString(R.string.error_5_camera_cant_connect_desc), 233, null);
                    }
                } else {
                    cam.stopPreview();
                }
                enabled = !enabled;
                new Handler().postDelayed(() -> isLoading = false, 500);
            }
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void destroy() {
        if (cam != null) {
            if (enabled)
                cam.stopPreview();
            cam.release();
        }
    }
}
