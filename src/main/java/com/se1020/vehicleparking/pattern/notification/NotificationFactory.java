package com.se1020.vehicleparking.pattern.notification;

import com.se1020.vehicleparking.model.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory Pattern: centralizes creation and normalization of Notification entities.
 */
@Component
public class NotificationFactory {

    public Notification create(UserNotificationEvent event) {
        if (event == null || isBlank(event.userId()) || isBlank(event.title()) || isBlank(event.message())) {
            return null;
        }

        String safeTitle = truncate(event.title().trim(), 120);
        String safeMessage = truncate(event.message().trim(), 500);
        String safeType = isBlank(event.type()) ? "SYSTEM" : truncate(event.type().trim().toUpperCase(), 30);
        String safeLink = isBlank(event.link()) ? null : truncate(event.link().trim(), 300);

        return new Notification(
                UUID.randomUUID().toString(),
                event.userId().trim(),
                safeTitle,
                safeMessage,
                safeType,
                safeLink,
                false,
                LocalDateTime.now().toString());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
