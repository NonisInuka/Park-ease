package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ParkingSessionJpaRepository extends JpaRepository<ParkingSession, String> {
    List<ParkingSession> findByUserId(String userId);
    List<ParkingSession> findByReservationId(String reservationId);
    List<ParkingSession> findByStatus(String status);
    List<ParkingSession> findBySlotIdAndStatusIn(String slotId, Collection<String> statuses);
    boolean existsBySlotIdAndStatusIn(String slotId, Collection<String> statuses);
    boolean existsByReservationIdAndStatusIn(String reservationId, Collection<String> statuses);
}
