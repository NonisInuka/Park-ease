package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.model.Vehicle;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.repository.ParkingSessionRepository;
import com.se1020.vehicleparking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final int MIN_DURATION_HOURS = 1;
    private static final int MAX_DURATION_HOURS = 24;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private NotificationEventPublisher notificationPublisher;

    // --- Basic field validation helpers (unchanged style from before) ---

    private boolean isValidVehiclePlate(String plate) {
        return plate != null && !plate.trim().isEmpty();
    }

    private boolean isValidDurationHours(int duration) {
        return duration >= MIN_DURATION_HOURS && duration <= MAX_DURATION_HOURS;
    }

    /** Parses "yyyy-MM-dd" + "HH:mm" (what the <input type=date>/<input type=time> fields submit). */
    private LocalDateTime parseStart(String reservationDate, String startTime) {
        try {
            LocalDate date = LocalDate.parse(reservationDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(startTime.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByUser(String userId) {
        return reservationRepository.findByUserId(userId);
    }

    public Reservation getReservationById(String reservationId) {
        return reservationRepository.findById(reservationId);
    }

    /**
     * Parking-operations lookup used when staff verify an arriving vehicle/reservation.
     * An exact reservation ID is preferred; otherwise an exact, case-insensitive vehicle plate
     * lookup returns that vehicle's reservations with the most recent booking first.
     */
    public List<Reservation> findForOperationsVerification(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String value = query.trim();
        Reservation exactReservation = reservationRepository.findById(value);
        if (exactReservation != null) {
            return List.of(exactReservation);
        }

        List<Reservation> matches = new ArrayList<>(
                reservationRepository.findByVehiclePlateIgnoreCase(value));
        matches.sort(Comparator.comparing((Reservation reservation) -> {
            LocalDateTime start = parseStart(reservation.getReservationDate(), reservation.getStartTime());
            return start != null ? start : LocalDateTime.MIN;
        }).reversed());
        return matches;
    }

    public boolean hasOpenParkingSession(String reservationId) {
        return reservationId != null && parkingSessionRepository.hasOpenSessionForReservation(reservationId);
    }

    /**
     * Returns the reservation only if it exists AND belongs to the given user - used for every
     * user-facing view/edit/cancel operation so a reservationId taken from a URL/form can never
     * be used to see or touch someone else's reservation. Returns null otherwise (not found, or
     * not owned by this user) without distinguishing the two, so this can't be used to probe
     * which reservation IDs exist.
     */
    public Reservation getOwnReservation(String reservationId, String userId) {
        if (reservationId == null || userId == null) {
            return null;
        }
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null || !userId.equals(reservation.getUserId())) {
            return null;
        }
        return reservation;
    }

    /**
     * Two reservations overlap when newStart < existingEnd AND newEnd > existingStart. Only
     * ACTIVE reservations on the same slot are considered (CANCELLED ones no longer hold the
     * slot). When editing an existing reservation, pass its own reservationId as
     * excludeReservationId so it is never compared against itself.
     */
    private boolean hasConflict(String slotId, LocalDateTime newStart, LocalDateTime newEnd, String excludeReservationId) {
        List<Reservation> activeForSlot = reservationRepository.findBySlotIdAndStatus(slotId, "ACTIVE");
        for (Reservation existing : activeForSlot) {
            if (excludeReservationId != null && excludeReservationId.equals(existing.getReservationId())) {
                continue;
            }
            LocalDateTime existingStart = parseStart(existing.getReservationDate(), existing.getStartTime());
            if (existingStart == null) {
                continue; // corrupt/unparseable legacy row - can't reason about it, skip rather than false-block
            }
            LocalDateTime existingEnd = existingStart.plusHours(existing.getDurationHours());

            boolean overlaps = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    /**
     * A vehicle cannot be in two places at once: reject a booking whose time window overlaps another
     * ACTIVE reservation for the same vehicle (same owner + plate), even on a different slot.
     */
    private boolean hasVehicleConflict(String userId, String vehiclePlate, LocalDateTime newStart,
                                       LocalDateTime newEnd, String excludeReservationId) {
        for (Reservation existing : reservationRepository.findByVehiclePlateIgnoreCase(vehiclePlate.trim())) {
            if (!"ACTIVE".equals(existing.getStatus()) || !userId.equals(existing.getUserId())) {
                continue;
            }
            if (excludeReservationId != null && excludeReservationId.equals(existing.getReservationId())) {
                continue;
            }
            LocalDateTime existingStart = parseStart(existing.getReservationDate(), existing.getStartTime());
            if (existingStart == null) {
                continue;
            }
            LocalDateTime existingEnd = existingStart.plusHours(existing.getDurationHours());
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a reservation after validating the fields, the slot, and time-conflict rules.
     * @return null on success, or a user-facing error message explaining why it was rejected.
     */
    public String createReservation(String userId, String vehiclePlate, String slotId,
                                     String reservationDate, String startTime, int durationHours) {
        if (userId == null || userId.trim().isEmpty()) {
            return "You must be logged in to book a slot.";
        }
        if (!isValidVehiclePlate(vehiclePlate)) {
            return "Please select a vehicle.";
        }
        Vehicle selectedVehicle = vehicleService.getOwnedVehicleByPlate(vehiclePlate, userId);
        if (selectedVehicle == null) {
            return "The selected vehicle does not belong to your account.";
        }
        if (slotId == null || slotId.trim().isEmpty()) {
            return "Please select a parking slot.";
        }
        if (!isValidDurationHours(durationHours)) {
            return "Duration must be between " + MIN_DURATION_HOURS + " and " + MAX_DURATION_HOURS + " hours.";
        }

        LocalDateTime newStart = parseStart(reservationDate, startTime);
        if (newStart == null) {
            return "Please provide a valid reservation date and start time.";
        }
        if (newStart.isBefore(LocalDateTime.now())) {
            return "Reservation date/time cannot be in the past.";
        }
        LocalDateTime newEnd = newStart.plusHours(durationHours);

        ParkingSlot slot = parkingSlotService.getSlotById(slotId);
        if (slot == null) {
            return "The selected parking slot no longer exists.";
        }
        if (!parkingSlotService.isOperationalForUse(slotId)) {
            return "The selected parking facility or zone is not currently available for booking.";
        }
        String slotCategory = VehicleCategories.migrateLegacy(slot.getType());
        String vehicleCategory = VehicleCategories.migrateLegacy(selectedVehicle.getType());
        if (!slotCategory.equals(vehicleCategory)) {
            return "The selected parking slot supports " + VehicleCategories.displayName(slotCategory)
                    + ", but your selected vehicle category is " + VehicleCategories.displayName(vehicleCategory) + ".";
        }
        // OCCUPIED represents current physical use, not future reservation availability.
        // A slot that is occupied now may still be reserved for a later non-overlapping time.
        if ("MAINTENANCE".equals(slot.getStatus())) {
            return "This parking slot is currently under maintenance and cannot be booked.";
        }
        if (!"AVAILABLE".equals(slot.getStatus()) && !"OCCUPIED".equals(slot.getStatus())) {
            return "This parking slot is not currently available for booking.";
        }

        if (hasConflict(slotId, newStart, newEnd, null)) {
            return "This parking slot is already reserved for an overlapping time period. Please choose a different time or slot.";
        }

        if (hasVehicleConflict(userId, vehiclePlate, newStart, newEnd, null)) {
            return "This vehicle already has a reservation that overlaps this time period.";
        }

        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, userId, vehiclePlate.trim(),
                slotId, slot.getSlotNumber(), reservationDate.trim(), startTime.trim(), durationHours, "ACTIVE");
        reservationRepository.save(reservation);
        notificationPublisher.notifyUser(userId, "Reservation confirmed",
                "Your reservation for slot " + slot.getSlotNumber() + " on " + reservationDate.trim()
                        + " at " + startTime.trim() + " has been confirmed.",
                "RESERVATION", "/user/reservations");
        return null;
    }

    /**
     * Updates the start time / duration of an existing ACTIVE reservation, re-running the same
     * conflict check (excluding the reservation being edited itself).
     * @return null on success, or a user-facing error message explaining why it was rejected.
     */
    public String updateReservation(String reservationId, String userId, String startTime, int durationHours) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "Reservation not found.";
        }
        if (!isValidDurationHours(durationHours)) {
            return "Duration must be between " + MIN_DURATION_HOURS + " and " + MAX_DURATION_HOURS + " hours.";
        }

        Reservation reservation = getOwnReservation(reservationId, userId);
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (!"ACTIVE".equals(reservation.getStatus())) {
            return "Only active reservations can be modified.";
        }
        if (parkingSessionRepository.hasOpenSessionForReservation(reservationId)) {
            return "This reservation cannot be modified after the vehicle has checked in.";
        }

        LocalDateTime newStart = parseStart(reservation.getReservationDate(), startTime);
        if (newStart == null) {
            return "Please provide a valid start time.";
        }
        if (newStart.isBefore(LocalDateTime.now())) {
            return "Reservation date/time cannot be in the past.";
        }
        LocalDateTime newEnd = newStart.plusHours(durationHours);

        if (hasConflict(reservation.getSlotId(), newStart, newEnd, reservationId)) {
            return "This parking slot is already reserved for an overlapping time period. Please choose a different time.";
        }

        if (hasVehicleConflict(userId, reservation.getVehiclePlate(), newStart, newEnd, reservationId)) {
            return "This vehicle already has a reservation that overlaps this time period.";
        }

        reservation.setStartTime(startTime.trim());
        reservation.setDurationHours(durationHours);
        reservationRepository.update(reservation);
        notificationPublisher.notifyUser(userId, "Reservation updated",
                "Your reservation for slot " + reservation.getSlotNumber() + " was updated to start at "
                        + startTime.trim() + " for " + durationHours + " hour(s).",
                "RESERVATION", "/user/reservations");
        return null;
    }

    /**
     * @return null on success, or a user-facing error message.
     */
    public String cancelReservation(String reservationId, String userId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "Reservation not found.";
        }

        Reservation reservation = getOwnReservation(reservationId, userId);
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (!"ACTIVE".equals(reservation.getStatus())) {
            return "This reservation is already " + reservation.getStatus().toLowerCase() + ".";
        }
        if (parkingSessionRepository.hasOpenSessionForReservation(reservationId)) {
            return "This reservation cannot be cancelled after the vehicle has checked in.";
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.update(reservation);
        notificationPublisher.notifyUser(userId, "Reservation cancelled",
                "Your reservation for slot " + reservation.getSlotNumber() + " has been cancelled.",
                "RESERVATION", "/user/reservations");
        return null;
    }

    /**
     * Marks a reservation completed when its actual parking session is checked out. This is a
     * system/parking-operations transition, not a user-facing edit.
     */
    public void completeReservationFromSession(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return;
        }
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation != null && "ACTIVE".equals(reservation.getStatus())) {
            reservation.setStatus("COMPLETED");
            reservationRepository.update(reservation);
        }
    }

    /**
     * If an incorrect COMPLETED session is removed while the original reservation window is
     * still relevant, restore that reservation to ACTIVE so it can be checked in correctly.
     * Historical reservations whose planned end is already in the past remain COMPLETED.
     */
    public void restoreReservationAfterIncorrectSessionRemoval(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return;
        }
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null || !"COMPLETED".equals(reservation.getStatus())) {
            return;
        }
        LocalDateTime start = parseStart(reservation.getReservationDate(), reservation.getStartTime());
        if (start != null && start.plusHours(reservation.getDurationHours()).isAfter(LocalDateTime.now())) {
            reservation.setStatus("ACTIVE");
            reservationRepository.update(reservation);
        }
    }

    // Hard delete - admin. Preserve parking-session history integrity.
    public String deleteReservation(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            return "Reservation not found.";
        }
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (!parkingSessionRepository.findByReservationId(reservationId).isEmpty()) {
            return "This reservation has parking-session history and cannot be deleted.";
        }
        reservationRepository.delete(reservationId);
        return null;
    }
}
