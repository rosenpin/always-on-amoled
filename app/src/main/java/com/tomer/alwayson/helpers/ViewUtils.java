package com.tomer.alwayson.helpers;

import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.widget.LinearLayout;

import com.tomer.alwayson.ContextConstatns;

public class ViewUtils implements ContextConstatns {
    private static DisplaySize displaySize;

    public static void move(Context context, LinearLayout mainView, boolean animate, String orientation, boolean isBig) {
        if (displaySize == null)
            displaySize = new DisplaySize(context);
        boolean vertical = orientation.equals("vertical");
        int height = displaySize.getHeight(vertical);
        int width = displaySize.getWidth(vertical);
        double multiplier = isBig ? 1.15 : 1.3;
        float position = vertical ? (float) (height - Utils.randInt(height / multiplier, height * multiplier)) : (float) (width - Utils.randInt(width / multiplier, width * multiplier));
        if (animate) {
            if (vertical)
                mainView.animate().translationY(position).setDuration(2000).setInterpolator(new FastOutSlowInInterpolator());
            else
                mainView.animate().translationX(position).setDuration(2000).setInterpolator(new FastOutSlowInInterpolator());
        } else {
            if (vertical)
                mainView.setY(position);
            else
                mainView.setX(position);
        }
    }
}

