package com.se1020.vehicleparking.pattern.pricing;

/** Result returned by a pricing strategy. */
public record ParkingFeeCalculation(int billableHours, double totalAmount) {
}
