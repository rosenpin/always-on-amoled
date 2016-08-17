package com.tomer.alwayson.helpers;

import android.content.Context;
import android.widget.Toast;

import com.tomer.alwayson.R;

public class UncoughtExcepction implements Thread.UncaughtExceptionHandler {
    Context context;

    public UncoughtExcepction(Context context) {
        this.context = context;
    }

    public void uncaughtException(Thread t, Throwable e) {
        Toast.makeText(context, R.string.error_0_unknown_error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }
}
