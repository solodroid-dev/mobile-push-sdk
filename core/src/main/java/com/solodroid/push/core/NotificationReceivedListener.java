package com.solodroid.push.core;

public interface NotificationReceivedListener {
    void onNotificationReceived(String title, String message, String image, String menuType, String detailsUrl, String link, String packageName, int currentPage, int totalPages);
}