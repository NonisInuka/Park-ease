package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.repository.jpa.BillJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so BillService/BillController do not need to change.
 * Backed by Spring Data JPA + SQL Server instead of JSON files.
 */
@Repository
public class BillRepository {

    @Autowired
    private BillJpaRepository billJpaRepository;

    public List<Bill> findAll() {
        return billJpaRepository.findAll();
    }

    public void saveAll(List<Bill> bills) {
        billJpaRepository.saveAll(bills);
    }

    public Bill findById(String billId) {
        return billJpaRepository.findById(billId).orElse(null);
    }

    public List<Bill> findByUserId(String userId) {
        return billJpaRepository.findByUserId(userId);
    }

    public List<Bill> findByReservationId(String reservationId) {
        return billJpaRepository.findByReservationId(reservationId);
    }

    public void save(Bill bill) {
        billJpaRepository.save(bill);
    }

    public void update(Bill updatedBill) {
        billJpaRepository.save(updatedBill);
    }

    public void delete(String billId) {
        if (billJpaRepository.existsById(billId)) {
            billJpaRepository.deleteById(billId);
        }
    }
}
