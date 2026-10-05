package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.model.SupportTicket;
import com.se1020.vehicleparking.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SupportTicketService {

    private static final int SUBJECT_MAX_LENGTH = 100;
    private static final int MESSAGE_MIN_LENGTH = 10;
    private static final int MESSAGE_MAX_LENGTH = 1000;
    private static final int ADMIN_RESPONSE_MAX_LENGTH = 1000;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private NotificationEventPublisher notificationPublisher;

    private boolean isValidSubject(String subject) {
        if (subject == null) return false;
        String value = subject.trim();
        return !value.isEmpty() && value.length() <= SUBJECT_MAX_LENGTH;
    }

    private boolean isValidMessage(String message) {
        if (message == null) return false;
        int length = message.trim().length();
        return length >= MESSAGE_MIN_LENGTH && length <= MESSAGE_MAX_LENGTH;
    }

    private boolean isValidAdminResponse(String response) {
        if (response == null) return false;
        String value = response.trim();
        return !value.isEmpty() && value.length() <= ADMIN_RESPONSE_MAX_LENGTH;
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    public List<SupportTicket> getTicketsByUser(String userId) {
        return supportTicketRepository.findByUserId(userId);
    }

    public SupportTicket getTicketById(String ticketId) {
        return supportTicketRepository.findById(ticketId);
    }

    // User submits a support request. Returns null on success, otherwise a user-facing error.
    public String submitTicket(String userId, String userName, String subject, String message) {
        if (userId == null || userId.trim().isEmpty() || userName == null || userName.trim().isEmpty()) {
            return "Unable to identify the current user.";
        }
        if (!isValidSubject(subject)) {
            return "Subject is required and must be 100 characters or fewer.";
        }
        if (!isValidMessage(message)) {
            return "Message must be between 10 and 1000 characters.";
        }

        String ticketId = UUID.randomUUID().toString();
        SupportTicket ticket = new SupportTicket(ticketId, userId, userName.trim(), subject.trim(),
                message.trim(), "OPEN", "", LocalDate.now().toString());
        supportTicketRepository.save(ticket);
        return null;
    }

    // Administrator may resolve only an OPEN ticket. Returns null on success.
    public String resolveTicket(String ticketId, String adminResponse) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            return "Support ticket not found.";
        }
        if (!isValidAdminResponse(adminResponse)) {
            return "Admin response is required and must be 1000 characters or fewer.";
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId);
        if (ticket == null) {
            return "Support ticket not found.";
        }
        if (!"OPEN".equals(ticket.getStatus())) {
            return "Only open support tickets can be resolved.";
        }

        ticket.setStatus("RESOLVED");
        ticket.setAdminResponse(adminResponse.trim());
        supportTicketRepository.update(ticket);
        notificationPublisher.notifyUser(ticket.getUserId(), "Support ticket resolved",
                "Your support ticket \"" + ticket.getSubject() + "\" has received a response.",
                "SUPPORT", "/user/support");
        return null;
    }

    // A user may close only their own ticket and only after an administrator resolved it.
    public String closeTicket(String ticketId, String userId) {
        if (ticketId == null || ticketId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            return "Support ticket not found.";
        }

        SupportTicket ticket = supportTicketRepository.findById(ticketId);
        if (ticket == null || !userId.equals(ticket.getUserId())) {
            // Deliberately use the same message for missing and non-owned tickets.
            return "Support ticket not found.";
        }
        if (!"RESOLVED".equals(ticket.getStatus())) {
            return "Only resolved support tickets can be closed.";
        }

        ticket.setStatus("CLOSED");
        supportTicketRepository.update(ticket);
        return null;
    }

    /** Deletes a ticket. Returns an error message, or null when it was deleted. */
    public String deleteTicket(String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            return "Support ticket not found.";
        }
        if (supportTicketRepository.findById(ticketId) == null) {
            return "Support ticket not found.";
        }
        supportTicketRepository.delete(ticketId);
        return null;
    }
}
