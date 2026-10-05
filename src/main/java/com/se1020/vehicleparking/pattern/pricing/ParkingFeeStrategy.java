package com.se1020.vehicleparking.pattern.pricing;

import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;

/** Strategy Pattern contract for parking fee calculation. */
public interface ParkingFeeStrategy {
    boolean supports(ParkingSession session);

    ParkingFeeCalculation calculate(Reservation reservation, ParkingSlot slot, ParkingSession session);
}
