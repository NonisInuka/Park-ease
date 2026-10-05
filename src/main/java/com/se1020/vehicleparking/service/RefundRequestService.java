package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.model.RefundRequest;
import com.se1020.vehicleparking.repository.BillRepository;
import com.se1020.vehicleparking.repository.RefundRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RefundRequestService {

    @Autowired private RefundRequestRepository refundRequestRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private BillService billService;
    @Autowired private NotificationEventPublisher notificationPublisher;

    public List<RefundRequest> getAllRefunds() { return refundRequestRepository.findAll(); }
    public List<RefundRequest> getRefundsByUser(String userId) { return refundRequestRepository.findByUserId(userId); }
    public RefundRequest getRefundById(String refundId) { return refundRequestRepository.findById(refundId); }

    public RefundRequest getLatestForBill(String billId) {
        List<RefundRequest> list = refundRequestRepository.findByBillId(billId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional
    public String requestRefund(String billId, String userId, String reason) {
        Bill bill = billService.getOwnBill(billId, userId);
        if (bill == null) return "Bill not found.";
        if (!"PAID".equals(bill.getStatus())) return "Only paid bills are eligible for a refund request.";
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 500)
            return "Refund reason must be between 10 and 500 characters.";
        if (billService.hasOpenRefund(billId)) return "A pending or approved refund already exists for this bill.";

        RefundRequest request = new RefundRequest(
                UUID.randomUUID().toString(), billId, userId, bill.getFinalAmount(),
                reason.trim(), "PENDING", LocalDateTime.now().toString());
        refundRequestRepository.save(request);
        notificationPublisher.notifyUser(userId, "Refund request submitted",
                "Your refund request for bill " + billId + " is pending review.",
                "REFUND", "/user/refunds");
        return null;
    }

    @Transactional
    public String processRefund(String refundId, String decision, String adminNote, String processedBy) {
        RefundRequest request = refundRequestRepository.findById(refundId);
        if (request == null) return "Refund request not found.";
        if (!"PENDING".equals(request.getStatus())) return "This refund request has already been processed.";
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) return "Invalid refund decision.";
        if (adminNote != null && adminNote.trim().length() > 500) return "Finance note cannot exceed 500 characters.";

        Bill bill = billRepository.findById(request.getBillId());
        if (bill == null) return "The bill linked to this refund no longer exists.";
        if ("APPROVED".equals(decision) && !"PAID".equals(bill.getStatus()))
            return "Only a currently paid bill can be refunded.";

        request.setStatus(decision);
        request.setProcessedAt(LocalDateTime.now().toString());
        request.setProcessedBy(processedBy);
        request.setAdminNote(adminNote == null ? null : adminNote.trim());
        refundRequestRepository.update(request);

        if ("APPROVED".equals(decision)) {
            // Development-safe stand-in for a real payment-gateway refund callback.
            bill.setStatus("REFUNDED");
            billRepository.update(bill);
        }

        notificationPublisher.notifyUser(request.getUserId(),
                "Refund request " + decision.toLowerCase(),
                "Your refund request for bill " + request.getBillId() + " was " + decision.toLowerCase() + ".",
                "REFUND", "/user/refunds");
        return null;
    }
}
