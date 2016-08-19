package com.heinrichreimersoftware.androidissuereporter.model;

import android.text.TextUtils;

import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo;

public class Report {
    private final String title;
    private final String description;
    private final DeviceInfo deviceInfo;
    private final ExtraInfo extraInfo;
    private final String email;

    public Report(String title, String description, DeviceInfo deviceInfo, ExtraInfo extraInfo, String email) {
        this.title = title;
        this.description = description;
        this.deviceInfo = deviceInfo;
        this.extraInfo = extraInfo;
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description + "\n\n"
                + "-\n\n"
                + deviceInfo.toMarkdown() + "\n\n"
                + extraInfo.toMarkdown()
                + (!TextUtils.isEmpty(email)
                ? "*Sent by [**" + email + "**](mailto:" + email + ")*"
                : "");
    }
}
