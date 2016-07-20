package com.tomer.alwayson.Views;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.tomer.alwayson.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import info.staticfree.android.twentyfourhour.overlay.DialOverlay;


/**
 * A widget that displays the time as a 12-at-the-top 24 hour analog clock. By
 * default, it will show the current time in the current timezone. The displayed
 * time can be set using {@link #setTime(long)} and and
 * {@link #setTimezone(TimeZone)}.
 *
 * @author <a href="mailto:steve@staticfree.info">Steve Pomeroy</a>
 */
public class Analog24HClock extends View {

    public static boolean is24;
    public static boolean hourOnTop;
    private final ArrayList<DialOverlay> mDialOverlay = new ArrayList<DialOverlay>();
    AttributeSet attributeSet;
    int defStyle;
    private Calendar mCalendar;
    private Drawable mFace;
    private int mDialWidth;
    private int mDialHeight;
    private int mBottom;
    private int mTop;
    private int mLeft;
    private int mRight;
    private boolean mSizeChanged;
    private HandsOverlay mHandsOverlay;

    public Analog24HClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public Analog24HClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public Analog24HClock(Context context) {
        super(context);

        init(context, null, 0);
    }

    public void init(Context context, AttributeSet attributeSet, int defStyle) {
        this.attributeSet = attributeSet;
        this.defStyle = defStyle;
        final TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.Analog24HClock, defStyle, 0);
        Drawable face = attrs.getDrawable(R.styleable.Analog24HClock_face);

        if (face != null) {
            setFace(face);
        } else {
            setFace(R.drawable.clock_face);
        }

        Drawable hourHand = attrs.getDrawable(R.styleable.Analog24HClock_hour_hand);
        if (hourHand == null) {
            hourHand = context.getResources().getDrawable(R.drawable.hour_hand);
        }

        Drawable minuteHand = attrs.getDrawable(R.styleable.Analog24HClock_minute_hand);
        if (minuteHand == null) {
            minuteHand = context.getResources().getDrawable(R.drawable.minute_hand);
        }

        mCalendar = Calendar.getInstance();

        mHandsOverlay = new HandsOverlay(hourHand, minuteHand);
    }

    public void init(Context context, int style, boolean is24) {
        Analog24HClock.is24 = is24;
        final TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.Analog24HClock, defStyle, 0);
        Drawable face = attrs.getDrawable(R.styleable.Analog24HClock_face);
        Drawable hourHand = attrs.getDrawable(R.styleable.Analog24HClock_hour_hand);
        Drawable minuteHand = attrs.getDrawable(R.styleable.Analog24HClock_minute_hand);

        switch (style) {
            case 0:
                Analog24HClock.hourOnTop = false;
                setFace(R.drawable.clock_face);
                hourHand = context.getResources().getDrawable(R.drawable.hour_hand);
                minuteHand = context.getResources().getDrawable(R.drawable.minute_hand);
                break;
            case 1:
                Analog24HClock.hourOnTop = false;
                setFace(com.tomer.alwayson.R.drawable.s7_face);
                hourHand = context.getResources().getDrawable(com.tomer.alwayson.R.drawable.s7_hour_hand);
                minuteHand = context.getResources().getDrawable(com.tomer.alwayson.R.drawable.s7_minute_hand);
                break;
            case 2:
                Analog24HClock.hourOnTop = true;
                setFace(com.tomer.alwayson.R.drawable.pebble_face);
                hourHand = context.getResources().getDrawable(com.tomer.alwayson.R.drawable.pebble_hour_hand);
                assert hourHand != null;
                hourHand.setAlpha(225);
                minuteHand = context.getResources().getDrawable(com.tomer.alwayson.R.drawable.pebble_minute_hand);
                break;
            case 3:
                Analog24HClock.hourOnTop = true;
                setFace(R.drawable.flat_face);
                hourHand = context.getResources().getDrawable(R.drawable.flat_hour_hand);
                assert hourHand != null;
                minuteHand = context.getResources().getDrawable(R.drawable.flat_minute_hand);
                break;
            case 4:
                Analog24HClock.hourOnTop = false;
                setFace(R.drawable.simple_face);
                hourHand = context.getResources().getDrawable(R.drawable.simple_hour_hand);
                assert hourHand != null;
                minuteHand = context.getResources().getDrawable(R.drawable.simple_minute_hand);
                break;
        }
        mCalendar = Calendar.getInstance();

        mHandsOverlay = new HandsOverlay(hourHand, minuteHand);
    }

    public void setFace(int drawableRes) {
        final Resources r = getResources();
        setFace(r.getDrawable(drawableRes));
    }

    public void setFace(Drawable face) {
        mFace = face;
        mSizeChanged = true;
        mDialHeight = mFace.getIntrinsicHeight();
        mDialWidth = mFace.getIntrinsicWidth();

        invalidate();
    }

    /**
     * Sets the currently displayed time in {@link System#currentTimeMillis()}
     * time.
     *
     * @param time the time to display on the clock
     */
    public void setTime(long time) {
        mCalendar.setTimeInMillis(time);

        invalidate();
    }

    /**
     * Sets the currently displayed time.
     *
     * @param calendar The time to display on the clock
     */
    public void setTime(Calendar calendar) {
        mCalendar = calendar;

        invalidate();
    }

    /**
     * When set, the minute hand will move slightly based on the current number
     * of seconds. If false, the minute hand will snap to the minute ticks.
     * Note: there is no second hand, this only affects the minute hand.
     *
     * @param showSeconds
     */
    public void setShowSeconds(boolean showSeconds) {
        mHandsOverlay.setShowSeconds(showSeconds);
    }

    /**
     * Sets the timezone to use when displaying the time.
     *
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
        mCalendar = Calendar.getInstance(timezone);
    }

    public void setHandsOverlay(HandsOverlay handsOverlay) {
        mHandsOverlay = handsOverlay;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSizeChanged = true;
    }

    // some parts from AnalogClock.java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final boolean sizeChanged = mSizeChanged;
        mSizeChanged = false;

        final int availW = mRight - mLeft;
        final int availH = mBottom - mTop;

        final int cX = availW / 2;
        final int cY = availH / 2;

        final int w = mDialWidth;
        final int h = mDialHeight;

        boolean scaled = false;

        if (availW < w || availH < h) {
            scaled = true;
            final float scale = Math.min((float) availW / (float) w,
                    (float) availH / (float) h);
            canvas.save();
            canvas.scale(scale, scale, cX, cY);
        }

        if (sizeChanged) {
            mFace.setBounds(cX - (w / 2), cY - (h / 2), cX + (w / 2), cY
                    + (h / 2));
        }

        mFace.draw(canvas);

        for (final DialOverlay overlay : mDialOverlay) {
            overlay.onDraw(canvas, cX, cY, w, h, mCalendar, sizeChanged);
        }

        mHandsOverlay.onDraw(canvas, cX, cY, w, h, mCalendar, sizeChanged);

        if (scaled) {
            canvas.restore();
        }
    }

    // from AnalogClock.java
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float) heightSize / (float) mDialHeight;
        }

        final float scale = Math.min(hScale, vScale);

        setMeasuredDimension(
                getDefaultSize((int) (mDialWidth * scale), widthMeasureSpec),
                getDefaultSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mDialHeight;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return mDialWidth;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // because we don't have access to the actual protected fields
        mRight = right;
        mLeft = left;
        mTop = top;
        mBottom = bottom;
    }


    public void addDialOverlay(DialOverlay dialOverlay) {
        mDialOverlay.add(dialOverlay);
    }

    public void removeDialOverlay(DialOverlay dialOverlay) {
        mDialOverlay.remove(dialOverlay);
    }

    public void clearDialOverlays() {
        mDialOverlay.clear();
    }
}

