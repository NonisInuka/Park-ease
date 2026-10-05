package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a vehicle's ACTUAL presence in the parking facility, from entry to exit -
 * distinct from Reservation, which only represents a booking (a plan to park, made in advance).
 *
 * Major Function 4 - Parking Operations & Session Management.
 * One ParkingSession is created per Reservation, at the moment Parking Staff checks the
 * vehicle in. Times are stored as ISO-8601 strings (LocalDateTime.toString()), matching the
 * date/time convention already used by Reservation/Bill elsewhere in this codebase.
 */
@Entity
@Table(name = "parking_sessions")
public class ParkingSession {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    private String reservationId;
    private String userId;
    private String vehiclePlate;
    private String slotId;
    private String slotNumber;

    private String checkInTime;          // set when Parking Staff records vehicle entry
    private String checkOutTime;         // set when Parking Staff records vehicle exit
    private String expectedEndTime;      // reservation's planned end time, used for overstay detection

    private String status; // ACTIVE, OVERSTAYED, COMPLETED

    public ParkingSession() {}

    public ParkingSession(String sessionId, String reservationId, String userId, String vehiclePlate,
                           String slotId, String slotNumber, String checkInTime, String expectedEndTime,
                           String status) {
        this.sessionId = sessionId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehiclePlate = vehiclePlate;
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.checkInTime = checkInTime;
        this.expectedEndTime = expectedEndTime;
        this.status = status;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

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

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(String expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
