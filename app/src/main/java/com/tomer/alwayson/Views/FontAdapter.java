package com.tomer.alwayson.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tomer.alwayson.Prefs;


public class FontAdapter extends BaseAdapter {

    String[] items;
    Context context;
    Prefs prefs;

    public FontAdapter(Context context, int items) {
        this.context = context;
        this.items = context.getResources().getStringArray(items);
        prefs = new Prefs(context);
        prefs.apply();
    }

    public static Typeface getFontByNumber(Context context, int number) {
        switch (number) {
            case 0:
                return Typeface.DEFAULT;
            case 1:
                return Typeface.DEFAULT_BOLD;
            case 2:
                return Typeface.defaultFromStyle(Typeface.ITALIC);
            case 3:
                return Typeface.SERIF;
            case 4:
                return Typeface.SANS_SERIF;
            case 5:
                return Typeface.MONOSPACE;
            case 6:
                return Typeface.createFromAsset(context.getAssets(), "fonts/dotted.ttf");
            case 7:
                return Typeface.createFromAsset(context.getAssets(), "fonts/ralewaydots.ttf");
            case 8:
                return Typeface.createFromAsset(context.getAssets(), "fonts/ginga.ttf");
            case 9:
                return Typeface.createFromAsset(context.getAssets(), "fonts/grinched.ttf");
            case 10:
                return Typeface.createFromAsset(context.getAssets(), "fonts/parryhotter.ttf");
            case 11:
                return Typeface.createFromAsset(context.getAssets(), "fonts/snackpatrol.otf");
            case 12:
                return Typeface.createFromAsset(context.getAssets(), "fonts/trashco.ttf");
            case 13:
                return Typeface.createFromAsset(context.getAssets(), "fonts/homoarak.ttf");
            case 14:
                return Typeface.createFromAsset(context.getAssets(), "fonts/waltograph.ttf");
            case 15:
                return Typeface.createFromAsset(context.getAssets(), "fonts/halo3.ttf");
            case 16:
                return Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
        }
        return Typeface.DEFAULT;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Typeface font = getFontByNumber(context, position);
        if (view == null) {
            final LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
        }

        ((TextView) view.findViewById(android.R.id.text1)).setText(items[position]);
        ((TextView) view.findViewById(android.R.id.text1)).setTypeface(font);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            ((TextView) view.findViewById(android.R.id.text1)).setTextColor(context.getResources().getColor(android.R.color.primary_text_dark));
        else
            ((TextView) view.findViewById(android.R.id.text1)).setTextColor(context.getResources().getColor(android.R.color.primary_text_light));
        return view;
    }
}

