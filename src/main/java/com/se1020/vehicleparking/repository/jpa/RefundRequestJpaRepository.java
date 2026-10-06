package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRequestJpaRepository extends JpaRepository<RefundRequest, String> {
    List<RefundRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    List<RefundRequest> findByBillIdOrderByCreatedAtDesc(String billId);
    List<RefundRequest> findByStatusOrderByCreatedAtAsc(String status);
}
