package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @Column(name = "feedback_id", length = 64)
    private String feedbackId;
    private String userId;
    private String userName;
    private String type;    // FEEDBACK, COMPLAINT
    @Lob
    private String message;
    private int rating;     // 1-5
    private String status;  // PENDING, RESOLVED
    @Lob
    private String adminResponse;
    private String createdDate;

    public Feedback() {}

    public Feedback(String feedbackId, String userId, String userName, String type,
                    String message, int rating, String status,
                    String adminResponse, String createdDate) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.message = message;
        this.rating = rating;
        this.status = status;
        this.adminResponse = adminResponse;
        this.createdDate = createdDate;
    }

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}