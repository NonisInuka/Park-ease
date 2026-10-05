package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillJpaRepository extends JpaRepository<Bill, String> {
    List<Bill> findByUserId(String userId);
    List<Bill> findByReservationId(String reservationId);
}

