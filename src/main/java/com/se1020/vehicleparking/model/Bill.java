package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @Column(name = "bill_id", length = 64)
    private String billId;
    private String reservationId;
    private String userId;
    private String slotNumber;
    private String vehiclePlate;
    private int durationHours;
    private double ratePerHour;
    private double totalAmount;
    private double discount;
    private double finalAmount;
    private String status; // UNPAID, PAID, REFUNDED, VOIDED
    private String createdDate;

    // Phase 6 payment/audit fields. Sensitive card data is deliberately NOT stored.
    private String sessionId;
    private String paymentReference;
    private String paymentMethod;
    private String cardType;
    private String cardLast4;
    private String paidAt;
    private String voidedAt;

    @Column(length = 500)
    private String voidReason;

    public Bill() {}

    public Bill(String billId, String reservationId, String userId, String slotNumber,
                String vehiclePlate, int durationHours, double ratePerHour,
                double totalAmount, double discount, double finalAmount,
                String status, String createdDate) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.slotNumber = slotNumber;
        this.vehiclePlate = vehiclePlate;
        this.durationHours = durationHours;
        this.ratePerHour = ratePerHour;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public double getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(double ratePerHour) { this.ratePerHour = ratePerHour; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }

    public String getVoidedAt() { return voidedAt; }
    public void setVoidedAt(String voidedAt) { this.voidedAt = voidedAt; }

    public String getVoidReason() { return voidReason; }
    public void setVoidReason(String voidReason) { this.voidReason = voidReason; }
}
