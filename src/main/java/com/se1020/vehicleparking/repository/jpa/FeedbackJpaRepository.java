package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackJpaRepository extends JpaRepository<Feedback, String> {
    List<Feedback> findByUserId(String userId);
}
