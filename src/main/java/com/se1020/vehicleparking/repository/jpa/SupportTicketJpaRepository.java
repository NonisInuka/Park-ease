package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketJpaRepository extends JpaRepository<SupportTicket, String> {
    List<SupportTicket> findByUserId(String userId);
}
