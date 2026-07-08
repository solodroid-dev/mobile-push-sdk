package com.solodroid.push.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.solodroid.push.core.NotificationReceivedListener;
import com.solodroid.push.core.SolodroidPush;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebasePush";
    private static final String CHANNEL_ID = "universe_push_id_01";
    private static final String CHANNEL_NAME = "Universe Notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.d(TAG, "Message received from: " + message.getFrom());

        String title = "";
        String messageBody = "";
        String image = "";

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            messageBody = message.getNotification().getBody();
            if (message.getNotification().getImageUrl() != null) {
                image = message.getNotification().getImageUrl().toString();
            }
        }

        Map<String, String> data = message.getData();
        if (data.size() > 0) {
            if (title == null || title.isEmpty()) title = getDataValue(data, "title", "Notification");
            if (messageBody == null || messageBody.isEmpty()) messageBody = getDataValue(data, "message", "");
            if (image == null || image.isEmpty()) image = getDataValue(data, "image", "");
        }

        if ((title == null || title.isEmpty()) && (messageBody == null || messageBody.isEmpty())) return;

        // Callback ke Listener di MyApplication
        NotificationReceivedListener listener = SolodroidPush.getNotificationReceivedListener();
        if (listener != null) {
            listener.onNotificationReceived(
                    title, messageBody, image,
                    getDataValue(data, "menuType", ""),
                    getDataValue(data, "contentDetailsUrl", ""),
                    getDataValue(data, "externalLink", ""),
                    getDataValue(data, "packageName", ""),
                    parseSafeInt(data.get("currentPage"), 1),
                    parseSafeInt(data.get("totalPages"), 1)
            );
        }

        sendNotification(title, messageBody, image, data);
    }

    private void sendNotification(String title, String messageBody, String image, Map<String, String> data) {
        // --- PERBAIKAN NAVIGASI ---
        // Gunakan Action String yang sudah Mas definisikan di Manifest (OPEN_MAIN_ACTIVITY)
        // Agar tidak lari ke Splash Screen lagi.
        Intent intent = new Intent("android.intent.action.START");
        intent.setPackage(getPackageName()); // Pastikan hanya membuka app kita sendiri

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("isFromOneSignal", false);
        intent.putExtra("isFromNotification", true);
        intent.putExtra("title", title);
        intent.putExtra("message", messageBody);
        intent.putExtra("image", image);
        intent.putExtra("menuType", getDataValue(data, "menuType", ""));
        intent.putExtra("contentDetailsUrl", getDataValue(data, "contentDetailsUrl", ""));
        intent.putExtra("externalLink", getDataValue(data, "externalLink", ""));
        intent.putExtra("packageName", getDataValue(data, "packageName", ""));
        intent.putExtra("currentPage", parseSafeInt(data.get("currentPage"), 1));
        intent.putExtra("totalPages", parseSafeInt(data.get("totalPages"), 1));

        int requestCode = (int) System.currentTimeMillis();
        int pendingFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // --- PERBAIKAN BIG IMAGE ---
        // Kita download gambarnya jadi Bitmap supaya bisa tampil di Foreground
        if (image != null && !image.isEmpty()) {
            Bitmap bitmap = getBitmapFromUrl(image);
            if (bitmap != null) {
                builder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setSummaryText(messageBody));
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        notificationManager.notify(requestCode, builder.build());
    }

    // Helper untuk download gambar menjadi Bitmap
    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(TAG, "Error download image: " + e.getMessage());
            return null;
        }
    }

    private int getNotificationIcon() {
        int iconResId = getResources().getIdentifier("ic_stat_onesignal_default", "drawable", getPackageName());
        return (iconResId == 0) ? android.R.drawable.ic_dialog_info : iconResId;
    }

    private String getDataValue(Map<String, String> data, String key, String defaultValue) {
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }

    private int parseSafeInt(String value, int defaultValue) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}