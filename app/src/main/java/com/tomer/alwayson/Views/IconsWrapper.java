package com.tomer.alwayson.Views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tomer.alwayson.Globals;

import java.util.Map;

public class IconsWrapper extends LinearLayout {
    public IconsWrapper(Context context) {
        super(context);
    }

    public IconsWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconsWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void update(boolean state, int textColor) {
        if (Globals.notificationChanged && state) {
            removeAllViews();
            for (Map.Entry<String, Drawable> entry : Globals.notificationsDrawables.entrySet()) {
                Drawable drawable = entry.getValue();
                drawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
                ImageView icon = new ImageView(getContext());
                icon.setImageDrawable(drawable);
                icon.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
                FrameLayout.LayoutParams iconLayoutParams = new FrameLayout.LayoutParams(96, 96, Gravity.CENTER);
                icon.setPadding(12, 0, 12, 0);
                icon.setLayoutParams(iconLayoutParams);
                addView(icon);
            }
            Globals.notificationChanged = false;
        }
    }
}
