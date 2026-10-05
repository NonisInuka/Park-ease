package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * UC-01 (User & Account Management) support-ticket sub-flow: a driver can submit a support
 * request, an administrator reviews/resolves it, and the ticket is then closed.
 * Status lifecycle: OPEN -> RESOLVED -> CLOSED (matches the UC-01 activity diagram).
 */
@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @Column(name = "ticket_id", length = 64)
    private String ticketId;
    private String userId;
    private String userName;
    private String subject;
    @Lob
    private String message;
    private String status; // OPEN, RESOLVED, CLOSED
    @Lob
    private String adminResponse;
    private String createdDate;

    public SupportTicket() {}

    public SupportTicket(String ticketId, String userId, String userName, String subject,
                          String message, String status, String adminResponse, String createdDate) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.userName = userName;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.adminResponse = adminResponse;
        this.createdDate = createdDate;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
