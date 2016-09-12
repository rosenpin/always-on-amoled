package com.tomer.alwayson.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;


public class FontAdapter extends BaseAdapter {

    private String[] items;
    private Context context;

    public FontAdapter(Context context, int items) {
        this.context = context;
        this.items = context.getResources().getStringArray(items);
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
            case 17:
                return Typeface.createFromAsset(context.getAssets(), "fonts/pixel.ttf");
            case 18:
                return Typeface.createFromAsset(context.getAssets(), "fonts/roboto.ttf");
            case 19:
                return Typeface.createFromAsset(context.getAssets(), "fonts/roboto_thin.ttf");
            case 20:
                return Typeface.createFromAsset(context.getAssets(), "fonts/roboto_light.ttf");
            case 21:
                return Typeface.createFromAsset(context.getAssets(), "fonts/ritaglio.ttf");
            case 22:
                return Typeface.createFromAsset(context.getAssets(), "fonts/philippine.otf");
            case 23:
                return Typeface.createFromAsset(context.getAssets(), "fonts/some_time_later.otf");
            case 24:
                return Typeface.createFromAsset(context.getAssets(), "fonts/lcd.otf");
            case 25:
                return Typeface.createFromAsset(context.getAssets(), "fonts/black_chancery.ttf");
            case 26:
                return Typeface.createFromAsset(context.getAssets(), "fonts/ninja.ttf");
            case 27:
                return Typeface.createFromAsset(context.getAssets(), "fonts/still_time.ttf");
            case 28:
                return Typeface.createFromAsset(context.getAssets(), "fonts/bristol.otf");
            case 29:
                return Typeface.createFromAsset(context.getAssets(), "fonts/economica.ttf");
            case 30:
                return Typeface.createFromAsset(context.getAssets(), "fonts/porkys.ttf");
            case 31:
                return Typeface.createFromAsset(context.getAssets(), "fonts/quickhand.ttf");
            case 32:
                return Typeface.createFromAsset(context.getAssets(), "fonts/tr2n.ttf");
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
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
        }
        CheckedTextView item = (CheckedTextView) view.findViewById(android.R.id.text1);
        item.setText(items[position]);
        item.setTypeface(font);
        item.setTextColor(Color.WHITE);
        return view;
    }
}

