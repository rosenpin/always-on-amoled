package com.tomer.alwayson.Activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity;
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo;
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget;
import com.tomer.alwayson.Prefs;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SecretConstants;


public class ReporterActivity extends IssueReporterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setGuestEmailRequired(true);
    }

    @Override
    public GithubTarget getTarget() {
        return new GithubTarget("rosenpin", "AlwaysOnDisplayAmoled");
    }

    @Override
    public String getGuestToken() {
        return SecretConstants.getPropertyValue(this,"github-key");
    }

    @Override
    public void onSaveExtraInfo(ExtraInfo extraInfo) {
        Prefs prefs = new Prefs(this);
        String[][] preferences = prefs.toArray();
        for (String[] preference : preferences) {
            extraInfo.put(preference[0], preference[1]);
        }
    }
}
