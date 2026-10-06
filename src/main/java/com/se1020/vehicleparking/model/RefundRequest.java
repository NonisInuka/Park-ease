package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A refund request raised against a successfully paid bill.
 *
 * Phase 6 - Financial & Payment Management.
 * Refunds are intentionally modelled separately from Bill so the request/decision history is
 * retained even after the Bill status changes to REFUNDED.
 */
@Entity
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @Column(name = "refund_id", length = 64)
    private String refundId;

    private String billId;
    private String userId;
    private double amount;

    @Column(length = 500)
    private String reason;

    private String status; // PENDING, APPROVED, REJECTED
    private String createdAt;
    private String processedAt;
    private String processedBy;

    @Column(length = 500)
    private String adminNote;

    public RefundRequest() {}

    public RefundRequest(String refundId, String billId, String userId, double amount,
                         String reason, String status, String createdAt) {
        this.refundId = refundId;
        this.billId = billId;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getProcessedAt() { return processedAt; }
    public void setProcessedAt(String processedAt) { this.processedAt = processedAt; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
