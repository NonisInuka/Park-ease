package com.se1020.vehicleparking.pattern.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Subject in the Observer Pattern.
 *
 * Business services publish a user-facing event through notifyUser(...). The subject keeps
 * a collection of NotificationObserver implementations and calls update(...) on each one.
 * This explicit structure mirrors the classic Subject -> Observer pattern taught in lectures.
 */
@Component
public class NotificationEventPublisher implements NotificationSubject {

    private final List<NotificationObserver> observers = new ArrayList<>();

    /**
     * Spring supplies every NotificationObserver bean here. We register them through the same
     * addObserver(...) operation exposed by the Subject contract.
     */
    public NotificationEventPublisher(List<NotificationObserver> observers) {
        if (observers != null) {
            observers.forEach(this::addObserver);
        }
    }

    @Override
    public synchronized void addObserver(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(UserNotificationEvent event) {
        if (event == null) {
            return;
        }

        List<NotificationObserver> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(observers);
        }

        for (NotificationObserver observer : snapshot) {
            observer.update(event);
        }
    }

    public void notifyUser(String userId, String title, String message, String type, String link) {
        notifyObservers(new UserNotificationEvent(userId, title, message, type, link));
    }
}
