package com.se1020.vehicleparking.pattern.pricing;

import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Used for a completed parking stay. Actual time is rounded up to the next hour, while the
 * reserved duration remains the minimum charge.
 */
@Component
public class CompletedSessionPricingStrategy implements ParkingFeeStrategy {

    @Override
    public boolean supports(ParkingSession session) {
        return session != null && session.getCheckInTime() != null && session.getCheckOutTime() != null;
    }

    @Override
    public ParkingFeeCalculation calculate(Reservation reservation, ParkingSlot slot, ParkingSession session) {
        int reservedHours = Math.max(1, reservation.getDurationHours());
        int billableHours = reservedHours;

        try {
            LocalDateTime checkIn = LocalDateTime.parse(session.getCheckInTime());
            LocalDateTime checkOut = LocalDateTime.parse(session.getCheckOutTime());
            long minutes = Math.max(0, Duration.between(checkIn, checkOut).toMinutes());
            int actualRoundedUp = Math.max(1, (int) Math.ceil(minutes / 60.0));
            billableHours = Math.max(reservedHours, actualRoundedUp);
        } catch (Exception ignored) {
            // Keep the reserved-duration charge for malformed legacy session timestamps.
        }

        return new ParkingFeeCalculation(billableHours, billableHours * slot.getRatePerHour());
    }
}
