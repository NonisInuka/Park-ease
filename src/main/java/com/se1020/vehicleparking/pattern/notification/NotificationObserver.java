package com.se1020.vehicleparking.pattern.notification;

/**
 * Observer interface from the classic Observer Pattern.
 * Concrete observers implement update(...) to react when the subject publishes a notification event.
 */
public interface NotificationObserver {

    void update(UserNotificationEvent event);
}
