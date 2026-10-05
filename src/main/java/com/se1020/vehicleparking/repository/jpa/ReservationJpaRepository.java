package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByUserId(String userId);
    List<Reservation> findBySlotIdAndStatus(String slotId, String status);
    List<Reservation> findBySlotId(String slotId);
    List<Reservation> findByVehiclePlateIgnoreCase(String vehiclePlate);
}
