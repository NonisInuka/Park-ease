package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.RefundRequest;
import com.se1020.vehicleparking.repository.jpa.RefundRequestJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RefundRequestRepository {

    @Autowired
    private RefundRequestJpaRepository refundRequestJpaRepository;

    public List<RefundRequest> findAll() {
        return refundRequestJpaRepository.findAll();
    }

    public RefundRequest findById(String refundId) {
        return refundRequestJpaRepository.findById(refundId).orElse(null);
    }

    public List<RefundRequest> findByUserId(String userId) {
        return refundRequestJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<RefundRequest> findByBillId(String billId) {
        return refundRequestJpaRepository.findByBillIdOrderByCreatedAtDesc(billId);
    }

    public List<RefundRequest> findByStatus(String status) {
        return refundRequestJpaRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    public void save(RefundRequest refundRequest) {
        refundRequestJpaRepository.save(refundRequest);
    }

    public void update(RefundRequest refundRequest) {
        refundRequestJpaRepository.save(refundRequest);
    }
}
