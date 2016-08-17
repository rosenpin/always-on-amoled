package com.tomer.alwayson.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.helpers.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.views.FacesGridAdapter;

public class ClockPicker extends AppCompatActivity implements ContextConstatns{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Prefs prefs = new Prefs(this);
        prefs.apply();

        FacesGridAdapter facesGridAdapter = new FacesGridAdapter(this, 9);
        ((GridView) findViewById(R.id.grid)).setAdapter(facesGridAdapter);
    }
}
