package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.Notification;
import com.se1020.vehicleparking.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public long getUnreadCount(String userId) {
        if (userId == null || userId.isBlank()) return 0;
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void saveNotification(Notification notification) {
        if (notification != null) {
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public boolean markRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId);
        if (notification == null || userId == null || !userId.equals(notification.getUserId())) return false;
        if (!notification.isReadFlag()) {
            notification.setReadFlag(true);
            notificationRepository.save(notification);
        }
        return true;
    }

    @Transactional
    public void markAllRead(String userId) {
        if (userId == null || userId.isBlank()) return;
        for (Notification notification : notificationRepository.findByUserId(userId)) {
            if (!notification.isReadFlag()) {
                notification.setReadFlag(true);
                notificationRepository.save(notification);
            }
        }
    }

}
