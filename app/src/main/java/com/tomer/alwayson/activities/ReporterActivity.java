package com.tomer.alwayson.activities;

import android.app.NotificationManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.RadioButton;
import android.widget.TextView;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity;
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo;
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget;
import com.tomer.alwayson.AlwaysOnAMOLED;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SecretConstants;
import com.tomer.alwayson.helpers.Prefs;


public class ReporterActivity extends IssueReporterActivity {

    private String messageExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageExtra = getIntent().getStringExtra("log");
        if (messageExtra == null || messageExtra.isEmpty())
            setGuestEmailRequired(true);
        else {
            ((TextView) findViewById(R.id.air_inputTitle)).setText("Force close report");
            ((TextView) findViewById(R.id.air_inputDescription)).setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            ((TextView) findViewById(R.id.air_inputDescription)).setLines(8);
            ((TextView) findViewById(R.id.air_inputDescription)).setHorizontallyScrolling(false);
            ((TextView) findViewById(R.id.air_inputDescription)).setText(messageExtra);
            ((RadioButton) findViewById(R.id.air_optionAnonymous)).setChecked(true);
            NotificationManager nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nMgr.cancel(AlwaysOnAMOLED.reportNotificationID);
        }

    }

    @Override
    public GithubTarget getTarget() {
        return new GithubTarget("rosenpin", "AlwaysOnDisplayAmoled");
    }

    @Override
    public String getGuestToken() {
        return SecretConstants.getPropertyValue(this, "github-key");
    }

    @Override
    public void onSaveExtraInfo(ExtraInfo extraInfo) {
        Prefs prefs = new Prefs(this);
        String[][] preferences = prefs.toArray();
        for (String[] preference : preferences) {
            extraInfo.put(preference[0], preference[1]);
        }
        if (messageExtra != null)
            extraInfo.put("Error log", messageExtra);
    }
}
