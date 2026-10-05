package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.repository.ParkingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parking Operations & Session Management.
 *
 * A Reservation represents a planned booking. A ParkingSession represents the vehicle's actual
 * physical presence in a slot from check-in until check-out.
 */
@Service
public class ParkingSessionService {

    /** Allow a small operational grace period before the reservation's scheduled start. */
    public static final int EARLY_CHECK_IN_MINUTES = 30;

    @Autowired
    private ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private BillService billService;

    @Autowired
    private NotificationEventPublisher notificationPublisher;

    private LocalDateTime parseReservationStart(Reservation reservation) {
        try {
            LocalDate date = LocalDate.parse(reservation.getReservationDate().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(reservation.getStartTime().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime getExpectedEnd(Reservation reservation, LocalDateTime expectedStart) {
        return expectedStart.plusHours(reservation.getDurationHours());
    }

    /**
     * Central, non-mutating verification for vehicle entry. Returning null means the reservation
     * is currently valid for check-in; otherwise the returned message explains why entry should
     * not be recorded. Both the verification/search UI and the real check-in operation use this
     * same validation so staff never see a result that disagrees with the backend mutation path.
     */
    private String validateCheckIn(Reservation reservation, LocalDateTime now) {
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (!"ACTIVE".equals(reservation.getStatus())) {
            return "Only active reservations can be checked in.";
        }
        if (!parkingSessionRepository.findByReservationId(reservation.getReservationId()).isEmpty()) {
            return "This reservation has already been checked in.";
        }

        LocalDateTime expectedStart = parseReservationStart(reservation);
        if (expectedStart == null) {
            return "This reservation has an invalid date/time and cannot be checked in.";
        }
        LocalDateTime expectedEnd = getExpectedEnd(reservation, expectedStart);

        if (now.isBefore(expectedStart.minusMinutes(EARLY_CHECK_IN_MINUTES))) {
            return "Check-in opens " + EARLY_CHECK_IN_MINUTES + " minutes before the reservation start time.";
        }
        if (!now.isBefore(expectedEnd)) {
            return "This reservation's check-in window has already ended.";
        }

        ParkingSlot slot = parkingSlotService.getSlotById(reservation.getSlotId());
        if (slot == null) {
            return "The reservation's parking slot no longer exists.";
        }
        if (!parkingSlotService.isOperationalForUse(reservation.getSlotId())) {
            return "The parking facility or zone is not currently operational for check-in.";
        }
        if ("MAINTENANCE".equals(slot.getStatus())) {
            return "This parking slot is under maintenance and cannot be checked in.";
        }
        if (parkingSessionRepository.hasOpenSessionForSlot(reservation.getSlotId())) {
            return "This parking slot is currently occupied by another active parking session.";
        }
        if (!"AVAILABLE".equals(slot.getStatus())) {
            return "This parking slot is not physically available for check-in.";
        }
        return null;
    }

    /** Used by Parking Staff to verify a reservation before recording vehicle entry. */
    public String getCheckInEligibility(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "Reservation not found.";
        }
        return validateCheckIn(reservationService.getReservationById(reservationId), LocalDateTime.now());
    }

    /**
     * Only reservations inside the permitted check-in window are shown as ready. Future bookings
     * no longer appear as immediately check-in-able, and physically unavailable slots are skipped.
     */
    public List<Reservation> getReservationsReadyForCheckIn() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> ready = new ArrayList<>();

        for (Reservation reservation : reservationService.getAllReservations()) {
            if (validateCheckIn(reservation, now) == null) {
                ready.add(reservation);
            }
        }
        return ready;
    }

    /**
     * Records vehicle entry and marks the physical slot OCCUPIED.
     * @return null on success, otherwise a user-facing validation message.
     */
    @Transactional
    public String checkIn(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "Reservation not found.";
        }

        Reservation reservation = reservationService.getReservationById(reservationId);
        LocalDateTime now = LocalDateTime.now();
        String validationError = validateCheckIn(reservation, now);
        if (validationError != null) {
            return validationError;
        }

        LocalDateTime expectedStart = parseReservationStart(reservation);
        LocalDateTime expectedEnd = getExpectedEnd(reservation, expectedStart);

        ParkingSession parkingSession = new ParkingSession(
                UUID.randomUUID().toString(),
                reservationId,
                reservation.getUserId(),
                reservation.getVehiclePlate(),
                reservation.getSlotId(),
                reservation.getSlotNumber(),
                now.toString(),
                expectedEnd.toString(),
                "ACTIVE"
        );

        parkingSessionRepository.save(parkingSession);
        parkingSlotService.updateSlotStatus(reservation.getSlotId(), "OCCUPIED");
        notificationPublisher.notifyUser(reservation.getUserId(), "Vehicle checked in",
                "Your vehicle " + reservation.getVehiclePlate() + " has checked in to slot "
                        + reservation.getSlotNumber() + ".",
                "PARKING", "/user/sessions");
        return null;
    }

    /**
     * Records vehicle exit, completes the matching reservation, and frees the slot only when doing
     * so would not overwrite an administrative MAINTENANCE state.
     */
    @Transactional
    public String checkOut(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return "Parking session not found.";
        }

        ParkingSession parkingSession = parkingSessionRepository.findById(sessionId);
        if (parkingSession == null) {
            return "Parking session not found.";
        }
        if ("COMPLETED".equals(parkingSession.getStatus())) {
            return "This session has already been checked out.";
        }
        if (!"ACTIVE".equals(parkingSession.getStatus()) && !"OVERSTAYED".equals(parkingSession.getStatus())) {
            return "Only active or overstayed sessions can be checked out.";
        }

        parkingSession.setCheckOutTime(LocalDateTime.now().toString());
        parkingSession.setStatus("COMPLETED");
        parkingSessionRepository.update(parkingSession);

        reservationService.completeReservationFromSession(parkingSession.getReservationId());
        // Phase 6: checkout creates or finalizes exactly one bill for this completed stay.
        billService.generateOrUpdateBillForCompletedSession(parkingSession);

        ParkingSlot slot = parkingSlotService.getSlotById(parkingSession.getSlotId());
        if (slot != null
                && "OCCUPIED".equals(slot.getStatus())
                && !parkingSessionRepository.hasOpenSessionForSlot(parkingSession.getSlotId())) {
            parkingSlotService.updateSlotStatus(parkingSession.getSlotId(), "AVAILABLE");
        }
        // If an administrator has put the slot into MAINTENANCE, deliberately preserve it.

        notificationPublisher.notifyUser(parkingSession.getUserId(), "Vehicle checked out",
                "Your parking session for vehicle " + parkingSession.getVehiclePlate()
                        + " is complete. Your final bill is ready if payment is required.",
                "PARKING", "/user/bills");
        return null;
    }

    /** Refresh ACTIVE -> OVERSTAYED based on the reservation's planned end time. */
    @Transactional
    public void refreshOverstayedSessions() {
        LocalDateTime now = LocalDateTime.now();
        for (ParkingSession parkingSession : parkingSessionRepository.findByStatus("ACTIVE")) {
            try {
                LocalDateTime expectedEnd = LocalDateTime.parse(parkingSession.getExpectedEndTime());
                if (now.isAfter(expectedEnd)) {
                    parkingSession.setStatus("OVERSTAYED");
                    parkingSessionRepository.update(parkingSession);
                    notificationPublisher.notifyUser(parkingSession.getUserId(), "Parking session overstayed",
                            "Your vehicle " + parkingSession.getVehiclePlate()
                                    + " has exceeded the reserved parking duration.",
                            "PARKING", "/user/sessions");
                }
            } catch (Exception ignored) {
                // Keep one malformed legacy row from breaking the whole operations screen.
            }
        }
    }

    /**
     * Controlled removal of an incorrect session record. Physical slot state is repaired when it
     * is safe to do so, and a prematurely-completed future reservation may be restored to ACTIVE.
     */
    @Transactional
    public String removeIncorrectSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return "Parking session not found.";
        }

        ParkingSession parkingSession = parkingSessionRepository.findById(sessionId);
        if (parkingSession == null) {
            return "Parking session not found.";
        }

        boolean wasCompleted = "COMPLETED".equals(parkingSession.getStatus());
        String slotId = parkingSession.getSlotId();
        String reservationId = parkingSession.getReservationId();

        if (wasCompleted) {
            String financialError = billService.prepareForIncorrectSessionRemoval(reservationId);
            if (financialError != null) return financialError;
        }

        parkingSessionRepository.delete(sessionId);

        ParkingSlot slot = parkingSlotService.getSlotById(slotId);
        if (slot != null
                && "OCCUPIED".equals(slot.getStatus())
                && !parkingSessionRepository.hasOpenSessionForSlot(slotId)) {
            parkingSlotService.updateSlotStatus(slotId, "AVAILABLE");
        }

        if (wasCompleted) {
            reservationService.restoreReservationAfterIncorrectSessionRemoval(reservationId);
        }
        return null;
    }

    public List<ParkingSession> getAllSessions() {
        return parkingSessionRepository.findAll();
    }

    public List<ParkingSession> getSessionsByUser(String userId) {
        refreshOverstayedSessions();
        return parkingSessionRepository.findByUserId(userId);
    }

    public ParkingSession getSessionById(String sessionId) {
        return parkingSessionRepository.findById(sessionId);
    }
}
