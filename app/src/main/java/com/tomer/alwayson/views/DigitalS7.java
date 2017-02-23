package com.tomer.alwayson.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tomer.alwayson.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DigitalS7 extends RelativeLayout {
    private Context context;
    private TextView batteryTV;
    private ImageView batteryIV;

    public DigitalS7(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(R.layout.s7_digital, null));
        batteryTV = (TextView) findViewById(R.id.s7_battery_percentage_tv);
        batteryIV = (ImageView) findViewById(R.id.s7_battery_percentage_icon);
    }

    public void init(Typeface font, float textSize, int textColor) {
        ((TextView) findViewById(R.id.s7_hour_tv)).setTypeface(font);
        ((TextView) findViewById(R.id.s7_date_tv)).setTypeface(font);
        ((TextView) findViewById(R.id.s7_minute_tv)).setTypeface(font);
        ((TextView) findViewById(R.id.s7_am_pm)).setTypeface(font);
        ((TextView) findViewById(R.id.s7_battery_percentage_tv)).setTypeface(font);

        ((TextView) findViewById(R.id.s7_hour_tv)).setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 9.5));
        ((TextView) findViewById(R.id.s7_date_tv)).setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 1));
        ((TextView) findViewById(R.id.s7_minute_tv)).setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 3.5));
        ((TextView) findViewById(R.id.s7_minute_tv)).setHeight((int) ((TextView) findViewById(R.id.s7_minute_tv)).getTextSize() + 35);
        ((TextView) findViewById(R.id.s7_am_pm)).setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 1.2));
        findViewById(R.id.s7_am_pm).setPadding((int) getResources().getDimension(R.dimen.small_spacing), (int) (textSize / 1.7), 0, 0);
        ((TextView) findViewById(R.id.s7_date_tv)).setMaxWidth((int) (textSize * 2.3));

        if (textColor != -1)
            ((TextView) findViewById(R.id.s7_minute_tv)).setTextColor(textColor);

        batteryTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (textSize * 0.2 * 1));
        ViewGroup.LayoutParams batteryIVlp = batteryIV.getLayoutParams();
        batteryIVlp.height = (int) (textSize * 0.8);
        batteryIV.setLayoutParams(batteryIVlp);
    }

    public void update(boolean showAmPm) {
        SimpleDateFormat sdf = DateFormat.is24HourFormat(context) ? new SimpleDateFormat("HH", Locale.getDefault()) : new SimpleDateFormat("h", Locale.getDefault());
        String hour = sdf.format(new Date());
        sdf = DateFormat.is24HourFormat(context) ? new SimpleDateFormat("mm", Locale.getDefault()) : new SimpleDateFormat("mm", Locale.getDefault());
        String minute = sdf.format(new Date());

        ((TextView) findViewById(R.id.s7_hour_tv)).setText(hour);
        ((TextView) findViewById(R.id.s7_minute_tv)).setText(minute);
        if (showAmPm)
            ((TextView) findViewById(R.id.s7_am_pm)).setText((new SimpleDateFormat("aa", Locale.getDefault()).format(new Date())));
    }

    public void setDate(String date) {
        ((TextView) findViewById(R.id.s7_date_tv)).setText(date);
    }

    public TextView getBatteryTV() {
        return batteryTV;
    }

    public ImageView getBatteryIV() {
        return batteryIV;
    }
}
