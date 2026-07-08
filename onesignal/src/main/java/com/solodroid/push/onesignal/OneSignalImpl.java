package com.solodroid.push.onesignal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.Continue; // Import helper Continue
import com.solodroid.push.core.PushProvider;
import com.solodroid.push.core.PushClickListener;
import org.json.JSONObject;

public class OneSignalImpl implements PushProvider {

    @Override
    public void init(Context context, String appId) {
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(context, appId);
    }

    @Override
    public void setOnNotificationClickListener(PushClickListener listener) {
        OneSignal.getNotifications().addClickListener(event -> {
            String title = event.getNotification().getTitle();
            String message = event.getNotification().getBody();
            JSONObject data = event.getNotification().getAdditionalData();
            if (data != null && listener != null) {
                listener.onClick(
                        title,
                        message,
                        data.optString("menu_type", "none"),
                        data.optString("contentDetailsUrl", ""),
                        data.optString("link", ""),
                        data.optString("package_name", ""),
                        data.optInt("currentPage", 1),
                        data.optInt("totalPages", 1)
                );
            }
        });
    }

    @Override
    public void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OneSignal.getNotifications().requestPermission(false, Continue.none());
        }
    }

    @Override
    public void sendTag(String key, String value) {
        // Menggunakan fungsi OneSignal SDK v5.x untuk mengirim tag
        OneSignal.getUser().addTag(key, value);
    }

    @Override
    public void handleIntent(Intent intent) {
        // Kosongkan! OneSignal v5 sudah menangani klik lewat listener internal di MyApplication.
        // Ini mencegah konflik dan looping pada OneSignal.
    }

}