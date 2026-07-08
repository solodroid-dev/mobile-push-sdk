package com.solodroid.push.firebase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.solodroid.push.core.PushClickListener;
import com.solodroid.push.core.PushProvider;

public class FirebasePushImpl implements PushProvider {

    private static PushClickListener sClickListener;

    @Override
    public void init(Context context, String appId) {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        Log.d("FirebasePush", "Firebase Init");

        // Tambahkan ini untuk cek token saat init
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String registrationToken = task.getResult();
                Log.i("FirebasePush", "Current Token: " + registrationToken);
            }
        });
    }

    @Override
    public void setOnNotificationClickListener(PushClickListener listener) {
        // Listener dari MyApplication disimpan di sini
        sClickListener = listener;
        Log.d("FirebasePush", "Listener attached from MyApplication");
    }

    @Override
    public void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void sendTag(String key, String value) {
        FirebaseMessaging.getInstance().subscribeToTopic(value);
    }

    @Override
    public void handleIntent(Intent intent) {
        if (intent == null || sClickListener == null) return;

        // Pakai flag unik Firebase agar tidak bentrok dengan OneSignal
        if (intent.hasExtra("isFromNotification")) {

            Log.d("FirebasePush", "Processing Firebase Intent...");

            // Kirim data ke listener di MyApplication
            sClickListener.onClick(
                    intent.getStringExtra("title"),
                    intent.getStringExtra("message"),
                    intent.getStringExtra("menuType"),
                    intent.getStringExtra("contentDetailsUrl"),
                    intent.getStringExtra("externalLink"),
                    intent.getStringExtra("packageName"),
                    intent.getIntExtra("currentPage", 1),
                    intent.getIntExtra("totalPages", 1)
            );

            // --- KUNCI ANTI-LOOP: HAPUS SEMUA EXTRA SETELAH DIPROSES ---
            intent.removeExtra("isFromNotification");
            intent.removeExtra("menuType");
            intent.removeExtra("contentDetailsUrl");
            intent.removeExtra("externalLink");
            Log.d("FirebasePush", "Intent consumed and cleared.");
        }
    }

}