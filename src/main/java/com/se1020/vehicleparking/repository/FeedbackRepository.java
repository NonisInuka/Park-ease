package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.Feedback;
import com.se1020.vehicleparking.repository.jpa.FeedbackJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so FeedbackService/FeedbackController do not need to change.
 * Backed by Spring Data JPA + SQL Server instead of JSON files.
 */
@Repository
public class FeedbackRepository {

    @Autowired
    private FeedbackJpaRepository feedbackJpaRepository;

    public List<Feedback> findAll() {
        return feedbackJpaRepository.findAll();
    }

    public void saveAll(List<Feedback> feedbacks) {
        feedbackJpaRepository.saveAll(feedbacks);
    }

    public Feedback findById(String feedbackId) {
        return feedbackJpaRepository.findById(feedbackId).orElse(null);
    }

    public List<Feedback> findByUserId(String userId) {
        return feedbackJpaRepository.findByUserId(userId);
    }

    public void save(Feedback feedback) {
        feedbackJpaRepository.save(feedback);
    }

    public void update(Feedback updatedFeedback) {
        feedbackJpaRepository.save(updatedFeedback);
    }

    public void delete(String feedbackId) {
        if (feedbackJpaRepository.existsById(feedbackId)) {
            feedbackJpaRepository.deleteById(feedbackId);
        }
    }
}
