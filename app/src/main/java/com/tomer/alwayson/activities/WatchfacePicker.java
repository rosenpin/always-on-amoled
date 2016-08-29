package com.tomer.alwayson.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.tomer.alwayson.receivers.BatteryReceiver;
import com.tomer.alwayson.views.Clock;

public class WatchfacePicker extends AppCompatActivity {

    CustomGrid gridAdapter;
    Prefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_picker);
        prefs = new Prefs(this);
        prefs.apply();
        gridAdapter = new CustomGrid(WatchfacePicker.this, getResources().getTextArray(R.array.customize_clock).length);
        ((GridView) findViewById(R.id.watchface_picker_grid)).setAdapter(gridAdapter);
        new Handler().postDelayed(() -> ((GridView) findViewById(R.id.watchface_picker_grid)).smoothScrollToPosition(prefs.clockStyle), 300);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_watchface_clock_desc);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        toolbar.setNavigationOnClickListener(item -> {
            gridAdapter.destroy();
            finish();
        });
    }

    private boolean shouldUpdate(int position) {
        return position <= 2 || Globals.ownedItems != null && Globals.ownedItems.size() > 0;
    }

    class CustomGrid extends BaseAdapter implements ContextConstatns {
        private Prefs prefs;
        private Activity mContext;
        private int length;
        private View selected;
        private BatteryReceiver batteryReceiver;

        public CustomGrid(Activity c, int length) {
            this.mContext = c;
            this.length = length;
            this.prefs = new Prefs(c);
            this.prefs.apply();
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.watch_picker_item, null);
            Clock analogClock = (Clock) view.findViewById(R.id.clock);
            TextView title = (TextView) view.findViewById(R.id.clock_name);
            title.setText(mContext.getResources().getTextArray(R.array.customize_clock)[position]);
            analogClock.setStyle(mContext, position, position == DIGITAL_CLOCK || position == S7_DIGITAL ? 40 : 40, prefs.textColor, prefs.showAmPm, Typeface.SANS_SERIF);
            if (position == S7_DIGITAL)
                if (analogClock.getDigitalS7() != null) {
                    analogClock.getDigitalS7().setDate(Utils.getDateText(mContext));
                    analogClock.getDigitalS7().getBatteryTV().setText("75%");
                }
            if (position <= ANALOG_CLOCK || (Globals.ownedItems != null && Globals.ownedItems.size() > 0)) {
                view.findViewById(R.id.pro_label).setVisibility(View.INVISIBLE);
            }
            if (position == prefs.clockStyle)
                select(view);

            view.setOnClickListener(v -> {
                if (shouldUpdate(position)) {
                    select(view);
                    prefs.setString(Prefs.KEYS.TIME_STYLE.toString(), String.valueOf(position));
                } else
                    PreferencesActivity.quicklyPromptToSupport(WatchfacePicker.this, Globals.mService, findViewById(android.R.id.content));
            });

            grid = view;
            return grid;
        }

        private void select(View view) {
            if (selected != null)
                selected.findViewById(R.id.item_wrapper).setBackgroundColor(Color.parseColor("#424242"));
            selected = view;
            view.findViewById(R.id.item_wrapper).setBackgroundColor(Color.parseColor("#455A64"));
        }

        public void destroy() {
            try {
                unregisterReceiver(batteryReceiver);
            } catch (Exception ignored) {
            }
        }
    }
}

