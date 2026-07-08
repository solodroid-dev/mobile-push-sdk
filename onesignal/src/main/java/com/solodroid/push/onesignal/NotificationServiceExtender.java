package com.solodroid.push.onesignal;

import android.util.Log;

import androidx.annotation.NonNull;

import com.onesignal.notifications.IDisplayableNotification;
import com.onesignal.notifications.INotificationReceivedEvent;
import com.onesignal.notifications.INotificationServiceExtension;
import com.solodroid.push.core.NotificationReceivedListener;
import com.solodroid.push.core.SolodroidPush;
import org.json.JSONObject;

public class NotificationServiceExtender implements INotificationServiceExtension {

    @Override
    public void onNotificationReceived(@NonNull INotificationReceivedEvent event) {
        try {
            IDisplayableNotification notification = event.getNotification();
            JSONObject data = notification.getAdditionalData();

            Log.d("SolodroidPush", "Extender Menyala! Menangkap notif: " + notification.getTitle());

            NotificationReceivedListener listener = SolodroidPush.getNotificationReceivedListener();
            if (listener != null) {

                // --- PERBAIKAN: Ambil URL Big Picture dari Payload OneSignal ---
                String imageUrl = notification.getBigPicture();

                listener.onNotificationReceived(
                        notification.getTitle() != null ? notification.getTitle() : "Notification",
                        notification.getBody() != null ? notification.getBody() : "",
                        imageUrl != null ? imageUrl : "", // <--- Masukkan imageUrl di sini
                        data != null ? data.optString("menu_type", "") : "",
                        data != null ? data.optString("contentDetailsUrl", "") : "",
                        data != null ? data.optString("link", "") : "",
                        data != null ? data.optString("package_name", "") : "",
                        data != null ? data.optInt("currentPage", 1) : 1,
                        data != null ? data.optInt("totalPages", 1) : 1
                );
                Log.d("SolodroidPush", "Berhasil melempar data ke Database via Listener!");
            } else {
                Log.e("SolodroidPush", "Listener Null! MyApplication belum terekseskusi sempurna.");
            }
        } catch (Exception e) {
            Log.e("SolodroidPush", "Gagal menangkap notifikasi: " + e.getMessage());
        }
    }
}