package com.tomer.alwayson.helpers;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class DisplaySize {
    private Point size;
    private int height, width;

    public DisplaySize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        size = new Point();
        display.getSize(size);
    }

    public int getWidth(boolean vertical) {
        if (width == 0)
            width = vertical ? size.x : size.y;
        return width;
    }

    public int getHeight(boolean vertical) {
        if (height == 0)
            height = vertical ? size.y : size.x;
        return height;
    }
}
