package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.repository.jpa.ReservationJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so ReservationService/ReservationController do not need to change.
 * Backed by Spring Data JPA + SQL Server instead of JSON files.
 */
@Repository
public class ReservationRepository {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    public void saveAll(List<Reservation> reservations) {
        reservationJpaRepository.saveAll(reservations);
    }

    public Reservation findById(String reservationId) {
        return reservationJpaRepository.findById(reservationId).orElse(null);
    }

    public List<Reservation> findByUserId(String userId) {
        return reservationJpaRepository.findByUserId(userId);
    }

    /** All reservations for a slot in a given status - used for overlap/conflict checks. */
    public List<Reservation> findBySlotIdAndStatus(String slotId, String status) {
        return reservationJpaRepository.findBySlotIdAndStatus(slotId, status);
    }

    public List<Reservation> findBySlotId(String slotId) {
        return reservationJpaRepository.findBySlotId(slotId);
    }

    public List<Reservation> findByVehiclePlateIgnoreCase(String vehiclePlate) {
        return reservationJpaRepository.findByVehiclePlateIgnoreCase(vehiclePlate);
    }

    public void save(Reservation reservation) {
        reservationJpaRepository.save(reservation);
    }

    public void update(Reservation updatedReservation) {
        reservationJpaRepository.save(updatedReservation);
    }

    public void delete(String reservationId) {
        if (reservationJpaRepository.existsById(reservationId)) {
            reservationJpaRepository.deleteById(reservationId);
        }
    }
}
