# Menjaga agar class reflection Mas tidak hilang saat rilis
-keep class com.solodroid.push.onesignal.OneSignalImpl { *; }

# Aturan bawaan OneSignal (biar user module nggak perlu input manual di app)
-keep class com.onesignal.** { *; }