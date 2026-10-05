package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.pattern.notification.NotificationEventPublisher;
import com.se1020.vehicleparking.pattern.pricing.ParkingFeeCalculation;
import com.se1020.vehicleparking.pattern.pricing.ParkingFeeCalculator;
import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.model.ParkingSession;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.RefundRequest;
import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.repository.BillRepository;
import com.se1020.vehicleparking.repository.ParkingSessionRepository;
import com.se1020.vehicleparking.repository.RefundRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class BillService {

    @Autowired private BillRepository billRepository;
    @Autowired private ReservationService reservationService;
    @Autowired private ParkingSlotService parkingSlotService;
    @Autowired private ParkingSessionRepository parkingSessionRepository;
    @Autowired private RefundRequestRepository refundRequestRepository;
    @Autowired private NotificationEventPublisher notificationPublisher;
    @Autowired private ParkingFeeCalculator parkingFeeCalculator;

    public List<Bill> getAllBills() { return billRepository.findAll(); }
    public List<Bill> getBillsByUser(String userId) {
        return billRepository.findByUserId(userId).stream()
                .filter(bill -> !"VOIDED".equals(bill.getStatus()))
                .toList();
    }
    public Bill getBillById(String billId) { return billRepository.findById(billId); }

    public Bill getOwnBill(String billId, String userId) {
        Bill bill = billRepository.findById(billId);
        return bill != null && userId != null && userId.equals(bill.getUserId()) ? bill : null;
    }

    private Bill findCurrentBillForReservation(String reservationId) {
        for (Bill bill : billRepository.findByReservationId(reservationId)) {
            if (!"VOIDED".equals(bill.getStatus())) return bill;
        }
        return null;
    }

    /**
     * Generates short, user-friendly bill numbers such as B001, B002, B003...
     * Existing legacy UUID bill IDs are migrated by DataSeeder at startup, so new bills
     * continue from the highest clean numeric bill ID already stored in SQL Server.
     */
    private synchronized String nextBillId() {
        int max = 0;
        for (Bill bill : billRepository.findAll()) {
            String id = bill.getBillId();
            if (id == null || !id.matches("B\\d+")) continue;
            try {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy IDs; DataSeeder handles their migration separately.
            }
        }
        return String.format("B%03d", max + 1);
    }

    private Bill buildBill(Reservation reservation, ParkingSlot slot, ParkingSession session) {
        ParkingFeeCalculation fee = parkingFeeCalculator.calculate(reservation, slot, session);
        Bill bill = new Bill(nextBillId(), reservation.getReservationId(), reservation.getUserId(),
                reservation.getSlotNumber(), reservation.getVehiclePlate(), fee.billableHours(), slot.getRatePerHour(),
                fee.totalAmount(), 0, fee.totalAmount(), "UNPAID", LocalDate.now().toString());
        if (session != null) bill.setSessionId(session.getSessionId());
        return bill;
    }

    /** Manual fallback for completed legacy reservations. Normally checkout creates/updates the bill automatically. */
    @Transactional
    public String generateBill(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) return "Reservation not found.";
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) return "Reservation not found.";
        if (!"COMPLETED".equals(reservation.getStatus())) return "A final bill can be generated only after the parking session is completed.";
        if (findCurrentBillForReservation(reservationId) != null) return "A bill already exists for this reservation.";

        ParkingSlot slot = parkingSlotService.getSlotById(reservation.getSlotId());
        if (slot == null) return "The reservation's parking slot no longer exists.";
        ParkingSession completed = parkingSessionRepository.findByReservationId(reservationId).stream()
                .filter(s -> "COMPLETED".equals(s.getStatus())).findFirst().orElse(null);
        Bill bill = buildBill(reservation, slot, completed);
        billRepository.save(bill);
        notificationPublisher.notifyUser(reservation.getUserId(), "Bill generated",
                "A bill of LKR " + String.format("%.2f", bill.getFinalAmount())
                        + " is ready for reservation " + reservation.getReservationId() + ".",
                "PAYMENT", "/user/bills");
        return null;
    }

    /** Called on checkout. Recalculates an existing UNPAID legacy bill or creates the final bill once. */
    @Transactional
    public void generateOrUpdateBillForCompletedSession(ParkingSession session) {
        if (session == null) return;
        Reservation reservation = reservationService.getReservationById(session.getReservationId());
        if (reservation == null) return;
        ParkingSlot slot = parkingSlotService.getSlotById(reservation.getSlotId());
        if (slot == null) return;

        Bill existing = findCurrentBillForReservation(reservation.getReservationId());
        ParkingFeeCalculation fee = parkingFeeCalculator.calculate(reservation, slot, session);
        int hours = fee.billableHours();
        double total = fee.totalAmount();

        if (existing == null) {
            Bill bill = buildBill(reservation, slot, session);
            billRepository.save(bill);
            notificationPublisher.notifyUser(reservation.getUserId(), "Bill ready for payment",
                    "Your final parking bill is LKR " + String.format("%.2f", bill.getFinalAmount()) + ".",
                    "PAYMENT", "/user/bills");
            return;
        }
        if (!"UNPAID".equals(existing.getStatus())) return; // Never rewrite a completed financial transaction.

        existing.setSessionId(session.getSessionId());
        existing.setDurationHours(hours);
        existing.setRatePerHour(slot.getRatePerHour());
        existing.setTotalAmount(total);
        if (existing.getDiscount() > total) existing.setDiscount(total);
        existing.setFinalAmount(total - existing.getDiscount());
        billRepository.update(existing);
    }

    @Transactional
    public String applyDiscount(String billId, double discount) {
        if (billId == null || billId.trim().isEmpty()) return "Bill not found.";
        if (Double.isNaN(discount) || Double.isInfinite(discount)) return "Please enter a valid discount amount.";
        if (discount < 0) return "Discount cannot be negative.";
        Bill bill = billRepository.findById(billId);
        if (bill == null) return "Bill not found.";
        if (!"UNPAID".equals(bill.getStatus())) return "Discounts can only be applied to unpaid bills.";
        if (discount > bill.getTotalAmount()) return "Discount cannot exceed the total amount.";
        bill.setDiscount(discount);
        bill.setFinalAmount(bill.getTotalAmount() - discount);
        billRepository.update(bill);
        return null;
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private boolean passesLuhn(String cardNumber) {
        int sum = 0; boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = cardNumber.charAt(i) - '0';
            if (alternate) { n *= 2; if (n > 9) n -= 9; }
            sum += n; alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean matchesCardType(String number, String cardType) {
        if ("VISA".equals(cardType)) return number.startsWith("4");
        if (!"MASTER".equals(cardType)) return false;
        if (number.length() < 4) return false;
        int first2 = Integer.parseInt(number.substring(0, 2));
        int first4 = Integer.parseInt(number.substring(0, 4));
        return (first2 >= 51 && first2 <= 55) || (first4 >= 2221 && first4 <= 2720);
    }

    private boolean expiryValid(String expiry) {
        if (expiry == null || !expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) return false;
        try {
            int month = Integer.parseInt(expiry.substring(0, 2));
            int year = 2000 + Integer.parseInt(expiry.substring(3, 5));
            return !YearMonth.of(year, month).isBefore(YearMonth.now());
        } catch (Exception e) { return false; }
    }

    /** Mock card payment. Full card number and CVV are validated in memory and never persisted. */
    @Transactional
    public String payBill(String billId, String userId, String cardholderName, String cardNumber,
                          String expiryDate, String cvv, String cardType) {
        Bill bill = getOwnBill(billId, userId);
        if (bill == null) return "Bill not found.";
        if (!"UNPAID".equals(bill.getStatus())) return "This bill is not available for payment.";
        if (cardholderName == null || cardholderName.trim().length() < 2 || cardholderName.trim().length() > 100)
            return "Please enter a valid cardholder name.";

        String digits = digitsOnly(cardNumber);
        if (digits.length() < 13 || digits.length() > 19 || !passesLuhn(digits))
            return "Please enter a valid card number.";
        if (!matchesCardType(digits, cardType)) return "The card number does not match the selected card type.";
        if (!expiryValid(expiryDate)) return "The card expiry date is invalid or has passed.";
        if (cvv == null || !cvv.matches("\\d{3,4}")) return "Please enter a valid CVV.";

        bill.setStatus("PAID");
        bill.setPaymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        bill.setPaymentMethod("CARD");
        bill.setCardType(cardType);
        bill.setCardLast4(digits.substring(digits.length() - 4));
        bill.setPaidAt(LocalDateTime.now().toString());
        billRepository.update(bill);
        notificationPublisher.notifyUser(userId, "Payment successful",
                "Payment " + bill.getPaymentReference() + " for LKR "
                        + String.format("%.2f", bill.getFinalAmount()) + " was successful.",
                "PAYMENT", "/user/bills/" + bill.getBillId() + "/receipt");
        return null;
    }

    public boolean hasOpenRefund(String billId) {
        for (RefundRequest refund : refundRequestRepository.findByBillId(billId)) {
            if ("PENDING".equals(refund.getStatus()) || "APPROVED".equals(refund.getStatus())) return true;
        }
        return false;
    }

    /**
     * Keeps session correction and financial history consistent. An unpaid auto-generated bill can
     * be voided when its completed session is removed as incorrect; a paid/refunded bill blocks
     * session removal because a completed financial transaction must not be silently invalidated.
     */
    @Transactional
    public String prepareForIncorrectSessionRemoval(String reservationId) {
        Bill bill = findCurrentBillForReservation(reservationId);
        if (bill == null) return null;
        if ("UNPAID".equals(bill.getStatus())) {
            bill.setStatus("VOIDED");
            bill.setVoidedAt(LocalDateTime.now().toString());
            bill.setVoidReason("Automatically voided because the related parking session was removed as incorrect.");
            billRepository.update(bill);
            return null;
        }
        if ("PAID".equals(bill.getStatus()) || "REFUNDED".equals(bill.getStatus())) {
            return "This parking session has completed financial history. Resolve the payment/refund record before removing the session.";
        }
        return null;
    }

    /** Incorrect financial records are voided instead of erasing audit history. */
    @Transactional
    public String voidBill(String billId, String reason) {
        Bill bill = billRepository.findById(billId);
        if (bill == null) return "Bill not found.";
        if (!"UNPAID".equals(bill.getStatus())) return "Only unpaid bills can be voided. Paid/refunded transactions must remain in financial history.";
        if (reason == null || reason.trim().length() < 5 || reason.trim().length() > 500)
            return "Please provide a reason between 5 and 500 characters.";
        if (hasOpenRefund(billId)) return "This bill has refund history and cannot be voided.";
        bill.setStatus("VOIDED");
        bill.setVoidedAt(LocalDateTime.now().toString());
        bill.setVoidReason(reason.trim());
        billRepository.update(bill);
        return null;
    }
}
