package com.tomer.alwayson.tasker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public final class EditActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", "Start the always on service");

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void finish()
    {
        super.finish();
    }

}