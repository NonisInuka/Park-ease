package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.repository.jpa.ParkingSessionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class ParkingSessionRepository {

    @Autowired
    private ParkingSessionJpaRepository parkingSessionJpaRepository;

    public List<ParkingSession> findAll() {
        return parkingSessionJpaRepository.findAll();
    }

    public ParkingSession findById(String sessionId) {
        return parkingSessionJpaRepository.findById(sessionId).orElse(null);
    }

    public List<ParkingSession> findByUserId(String userId) {
        return parkingSessionJpaRepository.findByUserId(userId);
    }

    public List<ParkingSession> findByReservationId(String reservationId) {
        return parkingSessionJpaRepository.findByReservationId(reservationId);
    }

    public List<ParkingSession> findByStatus(String status) {
        return parkingSessionJpaRepository.findByStatus(status);
    }

    public List<ParkingSession> findBySlotIdAndStatusIn(String slotId, Collection<String> statuses) {
        return parkingSessionJpaRepository.findBySlotIdAndStatusIn(slotId, statuses);
    }

    public boolean hasOpenSessionForSlot(String slotId) {
        return parkingSessionJpaRepository.existsBySlotIdAndStatusIn(slotId, List.of("ACTIVE", "OVERSTAYED"));
    }

    public boolean hasOpenSessionForReservation(String reservationId) {
        return parkingSessionJpaRepository.existsByReservationIdAndStatusIn(
                reservationId, List.of("ACTIVE", "OVERSTAYED"));
    }

    public void save(ParkingSession session) {
        parkingSessionJpaRepository.save(session);
    }

    public void update(ParkingSession session) {
        parkingSessionJpaRepository.save(session);
    }

    public void delete(String sessionId) {
        parkingSessionJpaRepository.deleteById(sessionId);
    }
}
