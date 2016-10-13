package com.tomer.alwayson.views;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.services.MainService;
import com.tomer.alwayson.services.NotificationListener;

import java.util.Map;

public class IconsWrapper extends LinearLayout {
    private MessageBox messageBox;
    private Context context;

    public IconsWrapper(Context context) {
        super(context);
        this.context = context;
    }

    public IconsWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public IconsWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void update(int textColor, final Runnable action) {
        removeAllViews();
        for (final Map.Entry<String, NotificationListener.NotificationHolder> entry : Globals.notifications.entrySet()) {
            Drawable drawable = entry.getValue().getIcon(context);
            if (drawable != null) {
                drawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
                final ImageView icon = new ImageView(getContext());
                icon.setImageDrawable(drawable);
                icon.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
                final LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(96, 96, Gravity.CENTER);
                icon.setPadding(12, 0, 12, 0);
                icon.setLayoutParams(iconLayoutParams);
                icon.setOnTouchListener((view, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        iconLayoutParams.width += 10;
                        iconLayoutParams.height += 10;
                        icon.setLayoutParams(iconLayoutParams);
                        if (iconLayoutParams.width > 400) {
                            if (entry.getValue().getIntent() != null) {
                                try {
                                    MainService.stoppedByShortcut = true;
                                    entry.getValue().getIntent().send();
                                    action.run();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                removeView(icon);
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        iconLayoutParams.width = 96;
                        iconLayoutParams.height = 96;
                        icon.setLayoutParams(iconLayoutParams);
                    } else {
                        Utils.logDebug("Event ", String.valueOf(event.getAction()));
                    }
                    return false;
                });
                icon.setOnClickListener(view -> messageBox.showNotification(entry.getValue()));
                addView(icon);
            }
        }
    }

    public void setMessageBox(MessageBox messageBox) {
        this.messageBox = messageBox;
    }
}
