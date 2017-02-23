package com.tomer.alwayson.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomer.alwayson.R;
import com.tomer.alwayson.services.NotificationListener;

public class MessageBox extends LinearLayout {

    private Context context;
    private CardView messageBox;
    private NotificationListener.NotificationHolder notification;

    public MessageBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void init(boolean horizontal) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(horizontal ? R.layout.message_box_horizontal : R.layout.message_box, null));
        messageBox = (CardView) findViewById(R.id.message_box);
    }

    public void showNotification(NotificationListener.NotificationHolder notification) {
        if (notification != null)
            if (!notification.getTitle().equals("null")) {
                this.notification = notification;
                //Clear previous animation
                if (messageBox.getAnimation() != null)
                    messageBox.clearAnimation();
                //Fade in animation
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(1000);
                //Fade out animation
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartOffset(40000);
                fadeOut.setDuration(1000);
                //Set the notification text and icon
                ((TextView) messageBox.findViewById(R.id.message_box_title)).setText(notification.getTitle());
                ((TextView) messageBox.findViewById(R.id.message_box_message)).setText(notification.getMessage());
                ((ImageView) messageBox.findViewById(R.id.message_box_icon)).setImageDrawable(notification.getIcon(context));
                ((TextView) messageBox.findViewById(R.id.message_app_name)).setText(notification.getAppName());
                //Run animations
                AnimationSet animation = new AnimationSet(false);
                animation.addAnimation(fadeIn);
                animation.addAnimation(fadeOut);
                messageBox.setAnimation(animation);
            }
    }

    public NotificationListener.NotificationHolder getCurrentNotification() {
        return notification;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        messageBox.setOnClickListener(onClickListener);
    }

    public void clearNotificationBox(){
        ((TextView) messageBox.findViewById(R.id.message_box_title)).setText("");
        ((TextView) messageBox.findViewById(R.id.message_box_message)).setText("");
        ((ImageView) messageBox.findViewById(R.id.message_box_icon)).setImageBitmap(null);
        ((TextView) messageBox.findViewById(R.id.message_app_name)).setText("");
    }
}
