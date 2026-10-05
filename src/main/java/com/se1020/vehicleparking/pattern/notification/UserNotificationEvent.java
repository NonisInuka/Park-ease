package com.se1020.vehicleparking.pattern.notification;

/**
 * Event published by business services when a user-facing notification is required.
 * The publishing service does not need to know how notifications are constructed or stored.
 */
public record UserNotificationEvent(
        String userId,
        String title,
        String message,
        String type,
        String link
) {
}
