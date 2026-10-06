package com.se1020.vehicleparking.pattern.notification;

import com.se1020.vehicleparking.model.Notification;
import com.se1020.vehicleparking.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer in the Observer Pattern.
 * It reacts to UserNotificationEvent updates from NotificationEventPublisher.
 */
@Component
public class NotificationEventListener implements NotificationObserver {

    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationFactory notificationFactory,
                                     NotificationService notificationService) {
        this.notificationFactory = notificationFactory;
        this.notificationService = notificationService;
    }

    @Override
    public void update(UserNotificationEvent event) {
        Notification notification = notificationFactory.create(event);
        if (notification != null) {
            notificationService.saveNotification(notification);
        }
    }
}
