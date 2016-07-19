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
    //Where should the issues go?
    //(http://github.com/username/repository)
    @Override
    public GithubTarget getTarget() {
        return new GithubTarget("rosenpin", "AlwaysOnDisplayAmoled");
    }

    //[Optional] Auth token to open issues if users don't have a GitHub account
    //You can register a bot account on GitHub and copy ist OAuth2 token here.
    @Override
    public String getGuestToken() {
        return SecretConstants.getPropertyValue(this,"github-key");
    }

    //[Optional] Include other relevant info in the bug report (like custom variables)
    @Override
    public void onSaveExtraInfo(ExtraInfo extraInfo) {
        Prefs prefs = new Prefs(this);
        String[][] preferences = prefs.toArray();
        for (String[] preference : preferences) {
            extraInfo.put(preference[0], preference[1]);
        }
    }

}
