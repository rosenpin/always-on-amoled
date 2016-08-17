package com.tomer.alwayson.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.helpers.Prefs;

public class FacesGridAdapter extends BaseAdapter implements ContextConstatns {
    private Context context;
    private int length;
    private Prefs prefs;

    public FacesGridAdapter(Context c, int length) {
        this.context = c;
        this.prefs = new Prefs(c);
        this.length = length;
    }

    public int getCount() {
        return length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        Log.d("Position", String.valueOf(i));
        Clock clock = new Clock(context, null);
        clock.setStyle(context, 2, prefs.textSize, prefs.textColor, prefs.showAmPm, FontAdapter.getFontByNumber(context, prefs.font));
        return clock;
    }
}