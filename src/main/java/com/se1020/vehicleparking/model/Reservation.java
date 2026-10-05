package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @Column(name = "reservation_id", length = 64)
    private String reservationId;
    private String userId;
    private String vehiclePlate;
    private String slotId;
    private String slotNumber;
    private String reservationDate;
    private String startTime;
    private int durationHours;
    private String status; // ACTIVE, COMPLETED, CANCELLED

    public Reservation() {}

    public Reservation(String reservationId, String userId, String vehiclePlate,
                       String slotId, String slotNumber, String reservationDate,
                       String startTime, int durationHours, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehiclePlate = vehiclePlate;
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.durationHours = durationHours;
        this.status = status;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getReservationDate() { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}