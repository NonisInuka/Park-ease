package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.Notification;
import com.se1020.vehicleparking.repository.jpa.NotificationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationRepository(NotificationJpaRepository notificationJpaRepository) {
        this.notificationJpaRepository = notificationJpaRepository;
    }

    public List<Notification> findByUserId(String userId) {
        return notificationJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification findById(String notificationId) {
        return notificationJpaRepository.findById(notificationId).orElse(null);
    }

    public void save(Notification notification) {
        notificationJpaRepository.save(notification);
    }

    public long countUnread(String userId) {
        return notificationJpaRepository.countByUserIdAndReadFlagFalse(userId);
    }
}
