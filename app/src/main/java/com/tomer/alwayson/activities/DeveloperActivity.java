package com.tomer.alwayson.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;

public class DeveloperActivity extends AppCompatActivity implements View.OnClickListener {

    @BindViews({R.id.twitter, R.id.google_plus, R.id.google_play, R.id.linkedin, R.id.github, R.id.patreon})
    List<LinearLayout> developerLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        ButterKnife.bind(this);
        for (LinearLayout developerLink : developerLinks) {
            developerLink.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        String url = "";
        switch (v.getId()) {
            case R.id.twitter:
                url = "https://twitter.com/Rosenpin";
                break;
            case R.id.google_plus:
                url = "https://plus.google.com/+TomerRosenfeld";
                break;
            case R.id.google_play:
                url = "https://play.google.com/store/apps/developer?id=Tomer%27s+apps";
                break;
            case R.id.linkedin:
                url = "https://www.linkedin.com/in/tomer-rosenfeld-0220366a";
                break;
            case R.id.github:
                url = "https://github.com/rosenpin";
                break;
            case R.id.patreon:
                url = "https://www.patreon.com/user?u=2966388";
                break;
        }
        Utils.openURL(this, url);
    }
}
