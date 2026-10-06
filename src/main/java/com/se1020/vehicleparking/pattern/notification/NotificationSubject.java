package com.se1020.vehicleparking.pattern.notification;

/**
 * Subject interface from the classic Observer Pattern.
 * It exposes operations to register/remove observers and notify all registered observers.
 */
public interface NotificationSubject {

    void addObserver(NotificationObserver observer);

    void removeObserver(NotificationObserver observer);

    void notifyObservers(UserNotificationEvent event);
}
