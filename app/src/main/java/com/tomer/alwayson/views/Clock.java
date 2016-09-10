package com.tomer.alwayson.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

public class Clock extends LinearLayout implements ContextConstatns {

    private DigitalS7 digitalS7;
    private CustomAnalogClock analogClock;
    private KillableTextClock textClock;

    public Clock(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(R.layout.clock, null));
    }

    public Clock(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(R.layout.clock, null));
    }

    public void setStyle(Context context, int clockStyle, float textSize, int textColor, boolean showAmPm, Typeface font) {
        LinearLayout clockWrapper = (LinearLayout) findViewById(R.id.clock_wrapper);
        analogClock = (CustomAnalogClock) clockWrapper.findViewById(R.id.custom_analog_clock);
        ViewGroup.LayoutParams lp = clockWrapper.findViewById(R.id.custom_analog_clock).getLayoutParams();
        float clockSize = textSize < 80 ? textSize : 80;
        lp.height = (int) (clockSize * 10);
        lp.width = (int) (clockSize * 9.5);
        switch (clockStyle) {
            case DISABLED:
                removeView(clockWrapper);
                break;
            case DIGITAL_CLOCK:
                textClock = (KillableTextClock) clockWrapper.findViewById(R.id.digital_clock);
                textClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                textClock.setTextColor(textColor);
                if (!showAmPm)
                    textClock.setFormat12Hour("h:mm");
                if (Utils.isAndroidNewerThanN()) {
                    textClock.setTextLocale(context.getResources().getConfiguration().getLocales().get(0));
                } else {
                    textClock.setTextLocale(context.getResources().getConfiguration().locale);
                }
                textClock.setTypeface(font);

                clockWrapper.removeView(clockWrapper.findViewById(R.id.custom_analog_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                break;
            case ANALOG_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.default_face, R.drawable.default_hour_hand, R.drawable.default_minute_hand, 225, false, false);
                break;
            case ANALOG24_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.clock_face, R.drawable.hour_hand, R.drawable.minute_hand, 0, true, false);
                break;
            case S7_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.s7_face, R.drawable.s7_hour_hand, R.drawable.s7_minute_hand, 0, false, false);
                break;
            case PEBBLE_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.pebble_face, R.drawable.pebble_hour_hand, R.drawable.pebble_minute_hand, 225, false, true);
                break;
            case S7_DIGITAL:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.custom_analog_clock));
                if (textSize > 90)
                    textSize = 90;
                Prefs prefs = new Prefs(context);
                prefs.apply();
                if (textSize < 50 && prefs.batteryStyle == 1)
                    textSize = 50;
                digitalS7 = (DigitalS7) findViewById(R.id.s7_digital);
                digitalS7.init(font, textSize, textColor);
                break;
            case FLAT_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.flat_face, R.drawable.flat_hour_hand, R.drawable.flat_minute_hand, 235, false, false);
                break;
            case FLAT_RED_CLOCK:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.flat_face, R.drawable.flat_red_hour_hand, R.drawable.flat_red_minute_hand, 0, false, false);
                break;
            case FLAT_STANDARD_TICKS:
                clockWrapper.removeView(clockWrapper.findViewById(R.id.digital_clock));
                clockWrapper.removeView(clockWrapper.findViewById(R.id.s7_digital));
                clockWrapper.findViewById(R.id.custom_analog_clock).setLayoutParams(lp);
                analogClock.init(context, R.drawable.standard_ticks_face, R.drawable.hour_hand, R.drawable.minute_hand, 0, false, false);
                break;
        }
    }

    public DigitalS7 getDigitalS7() {
        return this.digitalS7;
    }

    public CustomAnalogClock getAnalogClock() {
        return analogClock;
    }

    public KillableTextClock getTextClock() {
        return textClock;
    }

    public boolean isFull() {
        return digitalS7 != null;
    }
}
