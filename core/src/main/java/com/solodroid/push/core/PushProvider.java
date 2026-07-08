package com.solodroid.push.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public interface PushProvider {
    void init(Context context, String appId);

    void setOnNotificationClickListener(PushClickListener listener);

    void requestPermission(Activity activity);

    void sendTag(String key, String value);

    void handleIntent(Intent intent);
}