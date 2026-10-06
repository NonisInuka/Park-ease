package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "notification_id", length = 64)
    private String notificationId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 30)
    private String type; // RESERVATION, PARKING, PAYMENT, REFUND, SUPPORT, FEEDBACK, SYSTEM

    @Column(length = 300)
    private String link;

    @Column(name = "read_flag", nullable = false)
    private boolean readFlag;

    @Column(name = "created_at", nullable = false, length = 40)
    private String createdAt;

    public Notification() {}

    public Notification(String notificationId, String userId, String title, String message,
                        String type, String link, boolean readFlag, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.link = link;
        this.readFlag = readFlag;
        this.createdAt = createdAt;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isReadFlag() { return readFlag; }
    public void setReadFlag(boolean readFlag) { this.readFlag = readFlag; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
