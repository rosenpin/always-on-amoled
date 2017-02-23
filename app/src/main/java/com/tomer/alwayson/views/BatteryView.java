package com.tomer.alwayson.views;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomer.alwayson.R;
import com.tomer.alwayson.receivers.BatteryReceiver;

public class BatteryView extends LinearLayout {
    private BatteryReceiver batteryReceiver;
    private Context context;
    private int style;

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(R.layout.battery, null));
    }

    public void init(Context context, DigitalS7 digitalS7, int batteryStyle, boolean s7_digital, int textColor, float textSize, Typeface font) {
        this.context = context;
        this.style = batteryStyle;
        LinearLayout batteryWrapper = (LinearLayout) findViewById(R.id.battery_wrapper);
        switch (batteryStyle) {
            case 0:
                removeView(batteryWrapper);
                break;
            case 1:
                ImageView batteryIV = (ImageView) batteryWrapper.findViewById(R.id.battery_percentage_icon);
                TextView batteryTV = (TextView) batteryWrapper.findViewById(R.id.battery_percentage_tv);
                if (!s7_digital) {
                    batteryTV.setTextColor(textColor);
                    batteryIV.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

                    batteryTV.setTypeface(font);
                    batteryTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 1));
                    ViewGroup.LayoutParams batteryIVlp = batteryIV.getLayoutParams();
                    batteryIVlp.height = (int) (textSize);
                    batteryIV.setLayoutParams(batteryIVlp);
                } else
                    removeAllViews();
                batteryReceiver = new BatteryReceiver(s7_digital ? digitalS7.getBatteryTV() : batteryTV, s7_digital ? digitalS7.getBatteryIV() : batteryIV);
                context.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                break;
        }
    }

    public void destroy() {
        if (style == 1)
            context.unregisterReceiver(batteryReceiver);
    }
}
