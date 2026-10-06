package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.model.Feedback;
import com.se1020.vehicleparking.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {

    private static final int MESSAGE_MIN_LENGTH = 10;
    private static final int MESSAGE_MAX_LENGTH = 1000;
    private static final int ADMIN_RESPONSE_MAX_LENGTH = 1000;

    private final FeedbackRepository feedbackRepository;
    private final NotificationEventPublisher notificationPublisher;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           NotificationEventPublisher notificationPublisher) {
        this.feedbackRepository = feedbackRepository;
        this.notificationPublisher = notificationPublisher;
    }

    private boolean isValidMessage(String message) {
        if (message == null) return false;
        int length = message.trim().length();
        return length >= MESSAGE_MIN_LENGTH && length <= MESSAGE_MAX_LENGTH;
    }

    private boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    private boolean isValidType(String type) {
        return "FEEDBACK".equals(type) || "COMPLAINT".equals(type);
    }

    private boolean isValidAdminResponse(String response) {
        if (response == null) return false;
        int length = response.trim().length();
        return length >= 1 && length <= ADMIN_RESPONSE_MAX_LENGTH;
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public List<Feedback> getFeedbacksByUser(String userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public Feedback getFeedbackById(String feedbackId) {
        return feedbackRepository.findById(feedbackId);
    }

    public Feedback getOwnFeedback(String feedbackId, String userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId);
        return feedback != null && userId != null && userId.equals(feedback.getUserId()) ? feedback : null;
    }

    @Transactional
    public String submitFeedback(String userId, String userName, String type,
                                 String message, int rating) {
        if (userId == null || userId.isBlank() || userName == null || userName.isBlank()) {
            return "Unable to identify the current user.";
        }
        if (!isValidType(type)) return "Please select a valid feedback type.";
        if (!isValidMessage(message)) return "Message must be between 10 and 1000 characters.";
        if (!isValidRating(rating)) return "Rating must be between 1 and 5.";

        String feedbackId = UUID.randomUUID().toString();
        Feedback feedback = new Feedback(feedbackId, userId, userName.trim(), type,
                message.trim(), rating, "PENDING", "", LocalDate.now().toString());
        feedbackRepository.save(feedback);
        return null;
    }

    @Transactional
    public String updateFeedback(String feedbackId, String userId, String type, String message, int rating) {
        if (!isValidType(type)) return "Please select a valid feedback type.";
        if (!isValidMessage(message)) return "Message must be between 10 and 1000 characters.";
        if (!isValidRating(rating)) return "Rating must be between 1 and 5.";

        Feedback feedback = getOwnFeedback(feedbackId, userId);
        if (feedback == null) return "Feedback not found.";
        if (!"PENDING".equals(feedback.getStatus())) return "Only pending feedback can be edited.";

        feedback.setType(type);
        feedback.setMessage(message.trim());
        feedback.setRating(rating);
        feedbackRepository.update(feedback);
        return null;
    }

    @Transactional
    public String resolveFeedback(String feedbackId, String adminResponse) {
        if (!isValidAdminResponse(adminResponse)) {
            return "Admin response is required and must be 1000 characters or fewer.";
        }

        Feedback feedback = feedbackRepository.findById(feedbackId);
        if (feedback == null) return "Feedback not found.";
        if (!"PENDING".equals(feedback.getStatus())) return "Only pending feedback can be resolved.";

        feedback.setStatus("RESOLVED");
        feedback.setAdminResponse(adminResponse.trim());
        feedbackRepository.update(feedback);
        notificationPublisher.notifyUser(feedback.getUserId(), "Feedback response received",
                "An administrator has responded to your " + feedback.getType().toLowerCase() + ".",
                "FEEDBACK", "/user/feedbacks");
        return null;
    }

    /** Deletes feedback. Returns an error message, or null when it was deleted. */
    public String deleteFeedback(String feedbackId) {
        if (feedbackId == null || feedbackId.isBlank()) return "Feedback not found.";
        if (feedbackRepository.findById(feedbackId) == null) return "Feedback not found.";
        feedbackRepository.delete(feedbackId);
        return null;
    }
}
