package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.SupportTicket;
import com.se1020.vehicleparking.repository.jpa.SupportTicketJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so SupportTicketService/SupportTicketController do not need to change,
 * matching the same pattern as UserRepository/FeedbackRepository. Backed by Spring Data JPA
 * + SQL Server (table auto-created by ddl-auto=update, no migration needed).
 */
@Repository
public class SupportTicketRepository {

    @Autowired
    private SupportTicketJpaRepository supportTicketJpaRepository;

    public List<SupportTicket> findAll() {
        return supportTicketJpaRepository.findAll();
    }

    public SupportTicket findById(String ticketId) {
        return supportTicketJpaRepository.findById(ticketId).orElse(null);
    }

    public List<SupportTicket> findByUserId(String userId) {
        return supportTicketJpaRepository.findByUserId(userId);
    }

    public void save(SupportTicket ticket) {
        supportTicketJpaRepository.save(ticket);
    }

    public void update(SupportTicket updatedTicket) {
        supportTicketJpaRepository.save(updatedTicket);
    }

    public void delete(String ticketId) {
        if (supportTicketJpaRepository.existsById(ticketId)) {
            supportTicketJpaRepository.deleteById(ticketId);
        }
    }
}
