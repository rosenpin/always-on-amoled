package com.tomer.alwayson.Helpers;

import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.widget.LinearLayout;

import com.tomer.alwayson.ContextConstatns;

public class ViewUtils implements ContextConstatns {
    private static DisplaySize displaySize;

    public static void move(Context context, LinearLayout mainView, boolean animate, String orientation) {
        if (displaySize == null)
            displaySize = new DisplaySize(context);
        boolean vertical = orientation.equals("vertical");
        int height = displaySize.getHeight(vertical);
        int width = displaySize.getWidth(vertical);
        float position = vertical ? (float) (height - Utils.randInt(height / 1.3, height * 1.3)) : (float) (width - Utils.randInt(width / 1.3, width * 1.3));
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

