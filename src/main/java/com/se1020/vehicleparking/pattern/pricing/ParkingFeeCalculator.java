package com.se1020.vehicleparking.pattern.pricing;

import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy context. Spring injects all pricing strategies and this class selects the strategy
 * that supports the current billing situation.
 */
@Component
public class ParkingFeeCalculator {

    private final List<ParkingFeeStrategy> strategies;

    public ParkingFeeCalculator(List<ParkingFeeStrategy> strategies) {
        this.strategies = strategies;
    }

    public ParkingFeeCalculation calculate(Reservation reservation, ParkingSlot slot, ParkingSession session) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(session))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No parking pricing strategy is available."))
                .calculate(reservation, slot, session);
    }
}
