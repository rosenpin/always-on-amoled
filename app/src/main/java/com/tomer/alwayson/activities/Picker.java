package com.tomer.alwayson.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.helpers.Utils;
import com.tomer.alwayson.views.Clock;
import com.tomer.alwayson.views.DateView;

public class Picker extends AppCompatActivity implements ContextConstatns {

    private Prefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_picker);
        prefs = new Prefs(this);
        prefs.apply();
        int mode = getIntent().getIntExtra(GRID_TYPE, GRID_TYPE_CLOCK);
        CustomGridViewAdapter gridAdapter = new CustomGridViewAdapter(Picker.this, getResources().getTextArray(mode == GRID_TYPE_CLOCK ? R.array.customize_clock : R.array.customize_date).length, mode);
        ((GridView) findViewById(R.id.watchface_picker_grid)).setAdapter(gridAdapter);
        new Handler().postDelayed(() -> ((GridView) findViewById(R.id.watchface_picker_grid)).smoothScrollToPosition(prefs.clockStyle), 300);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_watchface_clock_desc);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        toolbar.setNavigationOnClickListener(item -> finish());
    }

    private boolean shouldUpdate(int position) {
        return position <= 2 || Globals.ownedItems != null && Globals.ownedItems.size() > 0;
    }

    class CustomGridViewAdapter extends BaseAdapter implements ContextConstatns {
        private Prefs prefs;
        private Activity context;
        private int length;
        private View selected;
        private int mode;

        CustomGridViewAdapter(Activity c, int length, int mode) {
            this.context = c;
            this.length = length;
            this.prefs = new Prefs(c);
            this.prefs.apply();
            this.mode = mode;
        }

        @Override
        public int getCount() {
            return length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            grid = mode == GRID_TYPE_CLOCK ? getClockView(position) : getCalendarView(position);
            return grid;
        }

        private View getClockView(int position) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.watch_picker_item, null);
            Clock analogClock = (Clock) view.findViewById(R.id.clock);
            TextView title = (TextView) view.findViewById(R.id.clock_name);
            title.setText(context.getResources().getTextArray(R.array.customize_clock)[position]);
            analogClock.setStyle(context, position, 40, prefs.textColor, prefs.showAmPm, Typeface.SANS_SERIF);
            if (position == S7_DIGITAL)
                if (analogClock.getDigitalS7() != null) {
                    analogClock.getDigitalS7().setDate(Utils.getDateText(context, true));
                    analogClock.getDigitalS7().getBatteryTV().setText("");
                    analogClock.getDigitalS7().getBatteryIV().setImageDrawable(null);
                    analogClock.getDigitalS7().update(prefs.showAmPm);
                    analogClock.getDigitalS7().findViewById(R.id.s7_date_tv).getLayoutParams().width = 150;
                }
            if (position <= ANALOG_CLOCK || (Globals.ownedItems != null && Globals.ownedItems.size() > 0))
                view.findViewById(R.id.pro_label).setVisibility(View.INVISIBLE);

            if (position == prefs.clockStyle)
                select(view);

            view.setOnClickListener(v -> {
                if (shouldUpdate(position)) {
                    select(view);
                    prefs.setString(Prefs.KEYS.TIME_STYLE.toString(), String.valueOf(position));
                } else
                    DonateActivity.quicklyPromptToSupport(Picker.this, findViewById(android.R.id.content));
            });
            return view;
        }

        private View getCalendarView(int position) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.date_picker_item, null);
            DateView dateView = (DateView) view.findViewById(R.id.date);
            TextView title = (TextView) view.findViewById(R.id.clock_name);
            title.setText(context.getResources().getTextArray(R.array.customize_date)[position]);
            dateView.setDateStyle(position, 90, prefs.textColor, Typeface.SANS_SERIF);
            dateView.update(Utils.getDateText(context, false));
            if (position == prefs.dateStyle)
                select(view);

            if (dateView.getCalendarView() == null)
                return new View(context);

            if (dateView.isFull())
                dateView.getCalendarView().setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                    select(view);
                    prefs.setString(Prefs.KEYS.DATE_STYLE.toString(), String.valueOf(position));
                });

            view.setOnClickListener(v -> {
                select(view);
                prefs.setString(Prefs.KEYS.DATE_STYLE.toString(), String.valueOf(position));
            });
            return view;
        }

        private void select(View view) {
            if (selected != null)
                selected.findViewById(R.id.item_wrapper).setBackgroundColor(Color.parseColor("#424242"));
            selected = view;
            view.findViewById(R.id.item_wrapper).setBackgroundColor(Color.parseColor("#455A64"));
        }
    }
}

