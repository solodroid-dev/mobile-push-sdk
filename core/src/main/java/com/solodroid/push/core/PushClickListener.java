package com.solodroid.push.core;

public interface PushClickListener {
    void onClick(String title, String message, String menuType, String detailsUrl, String externalLink, String packageName, int currentPage, int totalPages);
}