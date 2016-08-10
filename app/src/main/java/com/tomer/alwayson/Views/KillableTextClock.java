package com.tomer.alwayson.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;

public class KillableTextClock extends TextClock {
    public KillableTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            invalidate();
        }
    }
}
