package com.solodroid.push.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SolodroidPush {

    private static final String TAG = "SolodroidPush";
    // Ubah ke List agar bisa menampung OneSignal DAN Firebase sekaligus
    private static final List<PushProvider> activeProviders = new ArrayList<>();
    private static NotificationReceivedListener receivedListener;
    private static PushClickListener clickListener;

    /**
     * Check if any push module is available.
     * Logika diubah agar mengecek eksistensi masing-masing secara independen.
     */
    public static boolean isModuleAvailable() {
        boolean osFound = isClassAvailable("com.solodroid.push.onesignal.OneSignalImpl");
        boolean fcmFound = isClassAvailable("com.solodroid.push.firebase.FirebasePushImpl");

        if (osFound) Log.d(TAG, "Module Detection: OneSignal implementation found.");
        if (fcmFound) Log.d(TAG, "Module Detection: Firebase implementation found.");

        return osFound || fcmFound;
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Initialize all detected push providers.
     * Jika keduanya ada, keduanya akan di-inisialisasi.
     */
    public static void init(Context context, String appId) {
        activeProviders.clear();

        // 1. Coba Inisialisasi OneSignal
        try {
            Class<?> osClazz = Class.forName("com.solodroid.push.onesignal.OneSignalImpl");
            PushProvider osProvider = (PushProvider) osClazz.newInstance();
            osProvider.init(context, appId);
            activeProviders.add(osProvider);
            Log.i(TAG, "Initialization: OneSignal initialized successfully.");
        } catch (Exception e) {
            Log.v(TAG, "Initialization: OneSignal not found or failed to init.");
        }

        // 2. Coba Inisialisasi Firebase
        try {
            Class<?> fcmClazz = Class.forName("com.solodroid.push.firebase.FirebasePushImpl");
            PushProvider fcmProvider = (PushProvider) fcmClazz.newInstance();
            fcmProvider.init(context, appId);
            activeProviders.add(fcmProvider);
            Log.i(TAG, "Initialization: Firebase initialized successfully.");
        } catch (Exception e) {
            Log.v(TAG, "Initialization: Firebase not found or failed to init.");
        }

        if (activeProviders.isEmpty()) {
            Log.w(TAG, "Initialization: No push providers were initialized.");
        }
    }

    /**
     * Daftarkan click listener ke SEMUA provider yang aktif.
     */
    public static void setClickListener(PushClickListener listener) {
        clickListener = listener;
        for (PushProvider provider : activeProviders) {
            provider.setOnNotificationClickListener(listener);
            Log.d(TAG, "Listener: Click listener attached to " + provider.getClass().getSimpleName());
        }
    }

    public static PushClickListener getClickListener() {
        return clickListener;
    }

    /**
     * Minta permission melalui semua provider (biasanya sistem Android hanya memunculkan satu dialog).
     */
    public static void requestPermission(Activity activity) {
        for (PushProvider provider : activeProviders) {
            provider.requestPermission(activity);
        }
    }

    /**
     * Kirim Tag ke OneSignal (FCM akan mengabaikan jika tidak diimplementasi).
     */
    public static void sendTag(String key, String value) {
        for (PushProvider provider : activeProviders) {
            Log.d(TAG, "Tagging: Sending tag [" + key + " : " + value + "] to " + provider.getClass().getSimpleName());
            provider.sendTag(key, value);
        }
    }

    public static void setNotificationReceivedListener(NotificationReceivedListener listener) {
        receivedListener = listener;
        Log.d(TAG, "Listener: Notification received listener attached.");
    }

    public static NotificationReceivedListener getNotificationReceivedListener() {
        return receivedListener;
    }

    public static void handleIntent(Intent intent) {
        if (intent == null) return;
        // Gunakan copy list untuk menghindari concurrent modification
        List<PushProvider> providers = new ArrayList<>(activeProviders);
        for (PushProvider provider : providers) {
            provider.handleIntent(intent);
        }
    }
}