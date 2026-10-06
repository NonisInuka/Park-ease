package com.se1020.vehicleparking.pattern.pricing;

import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;
import org.springframework.stereotype.Component;

/**
 * Used when no completed parking-session timing is available. The reservation duration is billed.
 */
@Component
public class ReservedDurationPricingStrategy implements ParkingFeeStrategy {

    @Override
    public boolean supports(ParkingSession session) {
        return session == null || session.getCheckInTime() == null || session.getCheckOutTime() == null;
    }

    @Override
    public ParkingFeeCalculation calculate(Reservation reservation, ParkingSlot slot, ParkingSession session) {
        int hours = Math.max(1, reservation.getDurationHours());
        return new ParkingFeeCalculation(hours, hours * slot.getRatePerHour());
    }
}
