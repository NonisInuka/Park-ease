package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.*;
import com.se1020.vehicleparking.repository.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Phase 7 - Dashboard, Reporting & Analytics.
 *
 * Keeps reporting read-only and derives all metrics from the existing SQL Server-backed
 * repositories. No summary values are persisted, so dashboards always reflect current data.
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingFacilityRepository facilityRepository;
    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSessionRepository sessionRepository;
    private final BillRepository billRepository;
    private final RefundRequestRepository refundRepository;
    private final FeedbackRepository feedbackRepository;

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    public DashboardService(UserRepository userRepository,
                            VehicleRepository vehicleRepository,
                            ParkingFacilityRepository facilityRepository,
                            ParkingSlotRepository slotRepository,
                            ReservationRepository reservationRepository,
                            ParkingSessionRepository sessionRepository,
                            BillRepository billRepository,
                            RefundRequestRepository refundRepository,
                            FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.sessionRepository = sessionRepository;
        this.billRepository = billRepository;
        this.refundRepository = refundRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Map<String, Object> getAdminDashboardData() {
        List<User> users = userRepository.findAll();
        List<ParkingFacility> facilities = facilityRepository.findAll();
        List<ParkingSlot> slots = slotRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<ParkingSession> sessions = sessionRepository.findAll();
        List<Bill> bills = billRepository.findAll();
        List<RefundRequest> refunds = refundRepository.findAll();
        List<Feedback> feedbacks = feedbackRepository.findAll();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", users.size());
        data.put("activeUsers", users.stream().filter(User::isActive).count());
        data.put("totalFacilities", facilities.size());
        data.put("totalSlots", slots.size());

        long available = countSlots(slots, "AVAILABLE");
        long occupied = countSlots(slots, "OCCUPIED");
        long maintenance = countSlots(slots, "MAINTENANCE");
        long operational = Math.max(0, slots.size() - maintenance);
        data.put("availableSlots", available);
        data.put("occupiedSlots", occupied);
        data.put("maintenanceSlots", maintenance);
        data.put("occupancyRate", percentage(occupied, operational));

        data.put("activeReservations", countReservations(reservations, "ACTIVE"));
        data.put("completedReservations", countReservations(reservations, "COMPLETED"));
        data.put("cancelledReservations", countReservations(reservations, "CANCELLED"));
        data.put("activeSessions", sessions.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count());
        data.put("overstayedSessions", sessions.stream().filter(s -> "OVERSTAYED".equalsIgnoreCase(s.getStatus())).count());
        data.put("completedSessions", sessions.stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus())).count());

        double paidRevenue = bills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum();
        double refundedAmount = bills.stream()
                .filter(b -> "REFUNDED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum();
        double unpaidAmount = bills.stream()
                .filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum();
        data.put("paidRevenue", paidRevenue);
        data.put("refundedAmount", refundedAmount);
        data.put("unpaidAmount", unpaidAmount);
        data.put("pendingRefunds", refunds.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        data.put("averageRating", averageRating(feedbacks));

        data.put("reservationTrend", buildDailyReservationTrend(reservations, 7));
        data.put("revenueTrend", buildMonthlyRevenueTrend(bills, 6));
        data.put("facilityStats", buildFacilityStats(facilities, slots));
        data.put("recentReservations", reservations.stream()
                .sorted(Comparator.comparing(this::reservationStartSafe).reversed())
                .limit(5).toList());
        data.put("recentPayments", bills.stream()
                .filter(b -> b.getPaidAt() != null && !b.getPaidAt().isBlank())
                .sorted(Comparator.comparing((Bill b) -> parseDateTime(b.getPaidAt()), Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5).toList());
        return data;
    }

    public Map<String, Object> getUserDashboardData(String userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        List<ParkingSession> sessions = sessionRepository.findByUserId(userId);
        List<Bill> bills = billRepository.findByUserId(userId).stream()
                .filter(b -> !"VOIDED".equalsIgnoreCase(b.getStatus())).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("vehicleCount", vehicleRepository.findByOwnerId(userId).size());
        data.put("activeReservations", countReservations(reservations, "ACTIVE"));
        data.put("completedReservations", countReservations(reservations, "COMPLETED"));
        data.put("cancelledReservations", countReservations(reservations, "CANCELLED"));
        data.put("activeSessions", sessions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()) || "OVERSTAYED".equalsIgnoreCase(s.getStatus()))
                .count());
        data.put("unpaidBills", bills.stream().filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus())).count());
        data.put("unpaidAmount", bills.stream().filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum());
        data.put("paidTotal", bills.stream().filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum());
        data.put("refundedTotal", bills.stream().filter(b -> "REFUNDED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum());
        data.put("pendingRefunds", refundRepository.findByUserId(userId).stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        data.put("recentReservations", reservations.stream()
                .sorted(Comparator.comparing(this::reservationStartSafe).reversed())
                .limit(5).toList());
        data.put("nextReservation", reservations.stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !reservationStartSafe(r).isBefore(LocalDateTime.now()))
                .min(Comparator.comparing(this::reservationStartSafe))
                .orElse(null));
        return data;
    }

    public Map<String, Object> getReportData(String requestedStartDate, String requestedEndDate) {
        LocalDate end = parseDate(requestedEndDate);
        if (end == null) end = LocalDate.now();
        LocalDate start = parseDate(requestedStartDate);
        if (start == null) start = end.minusDays(29);
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        final LocalDate rangeStart = start;
        final LocalDate rangeEnd = end;
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> inRange(parseDate(r.getReservationDate()), rangeStart, rangeEnd)).toList();
        List<ParkingSession> sessions = sessionRepository.findAll().stream()
                .filter(s -> inRange(dateOfDateTime(s.getCheckInTime()), rangeStart, rangeEnd)).toList();
        List<Bill> bills = billRepository.findAll().stream()
                .filter(b -> inRange(billActivityDate(b), rangeStart, rangeEnd)).toList();
        List<RefundRequest> refunds = refundRepository.findAll().stream()
                .filter(r -> inRange(refundActivityDate(r), rangeStart, rangeEnd)).toList();
        List<Feedback> feedbacks = feedbackRepository.findAll().stream()
                .filter(f -> inRange(parseDate(f.getCreatedDate()), rangeStart, rangeEnd)).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", start.toString());
        data.put("endDate", end.toString());
        data.put("reservationCount", reservations.size());
        data.put("activeReservations", countReservations(reservations, "ACTIVE"));
        data.put("completedReservations", countReservations(reservations, "COMPLETED"));
        data.put("cancelledReservations", countReservations(reservations, "CANCELLED"));
        data.put("sessionCount", sessions.size());
        data.put("completedSessions", sessions.stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus())).count());
        data.put("overstayedSessions", sessions.stream().filter(s -> "OVERSTAYED".equalsIgnoreCase(s.getStatus())).count());
        data.put("averageStayHours", averageStayHours(sessions));

        double grossCollected = bills.stream()
                .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()) || "REFUNDED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum();
        double refundsApproved = refunds.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus()))
                .mapToDouble(RefundRequest::getAmount).sum();
        double unpaid = bills.stream().filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Bill::getFinalAmount).sum();
        data.put("grossCollected", grossCollected);
        data.put("refundsApproved", refundsApproved);
        data.put("netRevenue", Math.max(0, grossCollected - refundsApproved));
        data.put("unpaidAmount", unpaid);
        data.put("pendingRefunds", refunds.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count());
        data.put("voidedBills", bills.stream().filter(b -> "VOIDED".equalsIgnoreCase(b.getStatus())).count());
        data.put("averageRating", averageRating(feedbacks));
        data.put("feedbackCount", feedbacks.size());
        data.put("topSlots", buildTopSlots(reservations));
        data.put("reservationTrend", buildRangeReservationTrend(reservations, start, end));
        data.put("bills", bills);
        data.put("reservations", reservations);
        data.put("sessions", sessions);
        return data;
    }

    public String buildOperationsCsv(String requestedStartDate, String requestedEndDate) {
        Map<String, Object> report = getReportData(requestedStartDate, requestedEndDate);
        @SuppressWarnings("unchecked")
        List<Reservation> reservations = (List<Reservation>) report.get("reservations");
        @SuppressWarnings("unchecked")
        List<ParkingSession> sessions = (List<ParkingSession>) report.get("sessions");

        StringBuilder csv = new StringBuilder();
        csv.append("RESERVATIONS\n");
        csv.append("Reservation ID,Date,Start Time,Duration Hours,Vehicle,Slot,Status\n");
        for (Reservation r : reservations) {
            csv.append(csv(r.getReservationId())).append(',')
                    .append(csv(r.getReservationDate())).append(',')
                    .append(csv(r.getStartTime())).append(',')
                    .append(r.getDurationHours()).append(',')
                    .append(csv(r.getVehiclePlate())).append(',')
                    .append(csv(r.getSlotNumber())).append(',')
                    .append(csv(r.getStatus())).append('\n');
        }
        csv.append("\nPARKING SESSIONS\n");
        csv.append("Session ID,Reservation ID,Vehicle,Slot,Check In,Check Out,Expected End,Status\n");
        for (ParkingSession s : sessions) {
            csv.append(csv(s.getSessionId())).append(',')
                    .append(csv(s.getReservationId())).append(',')
                    .append(csv(s.getVehiclePlate())).append(',')
                    .append(csv(s.getSlotNumber())).append(',')
                    .append(csv(s.getCheckInTime())).append(',')
                    .append(csv(s.getCheckOutTime())).append(',')
                    .append(csv(s.getExpectedEndTime())).append(',')
                    .append(csv(s.getStatus())).append('\n');
        }
        return csv.toString();
    }

    public String buildFinancialCsv(String requestedStartDate, String requestedEndDate) {
        Map<String, Object> report = getReportData(requestedStartDate, requestedEndDate);
        @SuppressWarnings("unchecked")
        List<Bill> bills = (List<Bill>) report.get("bills");

        StringBuilder csv = new StringBuilder();
        csv.append("Bill ID,Reservation ID,Vehicle,Slot,Amount,Discount,Final Amount,Status,Created Date,Paid At,Payment Reference\n");
        for (Bill b : bills) {
            csv.append(csv(b.getBillId())).append(',')
                    .append(csv(b.getReservationId())).append(',')
                    .append(csv(b.getVehiclePlate())).append(',')
                    .append(csv(b.getSlotNumber())).append(',')
                    .append(b.getTotalAmount()).append(',')
                    .append(b.getDiscount()).append(',')
                    .append(b.getFinalAmount()).append(',')
                    .append(csv(b.getStatus())).append(',')
                    .append(csv(b.getCreatedDate())).append(',')
                    .append(csv(b.getPaidAt())).append(',')
                    .append(csv(b.getPaymentReference())).append('\n');
        }
        return csv.toString();
    }

    private List<Map<String, Object>> buildDailyReservationTrend(List<Reservation> reservations, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        return buildRangeReservationTrend(reservations, start, end);
    }

    private List<Map<String, Object>> buildRangeReservationTrend(List<Reservation> reservations, LocalDate start, LocalDate end) {
        long dayCount = Duration.between(start.atStartOfDay(), end.plusDays(1).atStartOfDay()).toDays();
        if (dayCount > 31) {
            Map<YearMonth, Long> counts = reservations.stream()
                    .map(r -> parseDate(r.getReservationDate()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(YearMonth::from, TreeMap::new, Collectors.counting()));
            List<Map<String, Object>> series = new ArrayList<>();
            YearMonth month = YearMonth.from(start);
            YearMonth last = YearMonth.from(end);
            while (!month.isAfter(last)) {
                long count = counts.getOrDefault(month, 0L);
                series.add(point(month.format(MONTH_LABEL), count, 0));
                month = month.plusMonths(1);
            }
            applyPercent(series);
            return series;
        }

        Map<LocalDate, Long> counts = reservations.stream()
                .map(r -> parseDate(r.getReservationDate()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Map<String, Object>> series = new ArrayList<>();
        LocalDate date = start;
        while (!date.isAfter(end)) {
            long count = counts.getOrDefault(date, 0L);
            series.add(point(date.format(SHORT_DATE), count, 0));
            date = date.plusDays(1);
        }
        applyPercent(series);
        return series;
    }

    private List<Map<String, Object>> buildMonthlyRevenueTrend(List<Bill> bills, int months) {
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1L);
        Map<YearMonth, Double> totals = new HashMap<>();
        for (Bill bill : bills) {
            if (!"PAID".equalsIgnoreCase(bill.getStatus())) continue;
            LocalDate date = dateOfDateTime(bill.getPaidAt());
            if (date != null) totals.merge(YearMonth.from(date), bill.getFinalAmount(), Double::sum);
        }
        List<Map<String, Object>> series = new ArrayList<>();
        YearMonth month = start;
        while (!month.isAfter(end)) {
            double amount = totals.getOrDefault(month, 0.0);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("label", month.format(DateTimeFormatter.ofPattern("MMM")));
            point.put("value", amount);
            point.put("display", String.format(Locale.US, "%.2f", amount));
            point.put("percent", 0);
            series.add(point);
            month = month.plusMonths(1);
        }
        double max = series.stream().mapToDouble(p -> ((Number) p.get("value")).doubleValue()).max().orElse(0);
        for (Map<String, Object> point : series) {
            double value = ((Number) point.get("value")).doubleValue();
            point.put("percent", max <= 0 ? 0 : Math.max(3, Math.round(value * 100.0 / max)));
        }
        return series;
    }

    private List<Map<String, Object>> buildFacilityStats(List<ParkingFacility> facilities, List<ParkingSlot> slots) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ParkingFacility facility : facilities) {
            List<ParkingSlot> facilitySlots = slots.stream()
                    .filter(s -> Objects.equals(facility.getFacilityId(), s.getFacilityId())).toList();
            long available = countSlots(facilitySlots, "AVAILABLE");
            long occupied = countSlots(facilitySlots, "OCCUPIED");
            long maintenance = countSlots(facilitySlots, "MAINTENANCE");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", facility.getName());
            item.put("status", facility.getStatus());
            item.put("total", facilitySlots.size());
            item.put("available", available);
            item.put("occupied", occupied);
            item.put("maintenance", maintenance);
            item.put("occupancyRate", percentage(occupied, Math.max(0, facilitySlots.size() - maintenance)));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildTopSlots(List<Reservation> reservations) {
        Map<String, Long> counts = reservations.stream()
                .filter(r -> r.getSlotNumber() != null)
                .collect(Collectors.groupingBy(Reservation::getSlotNumber, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("slot", e.getKey());
                    row.put("count", e.getValue());
                    return row;
                }).toList();
    }

    private Map<String, Object> point(String label, long value, long percent) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("label", label);
        point.put("value", value);
        point.put("percent", percent);
        return point;
    }

    private void applyPercent(List<Map<String, Object>> series) {
        long max = series.stream().mapToLong(p -> ((Number) p.get("value")).longValue()).max().orElse(0);
        for (Map<String, Object> point : series) {
            long value = ((Number) point.get("value")).longValue();
            point.put("percent", max == 0 ? 0 : Math.max(3, Math.round(value * 100.0 / max)));
        }
    }

    private long countSlots(List<ParkingSlot> slots, String status) {
        return slots.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).count();
    }

    private long countReservations(List<Reservation> reservations, String status) {
        return reservations.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).count();
    }

    private int percentage(long numerator, long denominator) {
        if (denominator <= 0) return 0;
        return (int) Math.round(numerator * 100.0 / denominator);
    }

    private double averageRating(List<Feedback> feedbacks) {
        return feedbacks.stream().filter(f -> f.getRating() > 0)
                .mapToInt(Feedback::getRating).average().orElse(0.0);
    }

    private double averageStayHours(List<ParkingSession> sessions) {
        return sessions.stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus()))
                .mapToDouble(s -> {
                    LocalDateTime in = parseDateTime(s.getCheckInTime());
                    LocalDateTime out = parseDateTime(s.getCheckOutTime());
                    if (in == null || out == null || out.isBefore(in)) return 0.0;
                    return Duration.between(in, out).toMinutes() / 60.0;
                }).filter(v -> v > 0).average().orElse(0.0);
    }

    private LocalDateTime reservationStartSafe(Reservation reservation) {
        try {
            return LocalDateTime.of(LocalDate.parse(reservation.getReservationDate()),
                    java.time.LocalTime.parse(reservation.getStartTime()));
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }

    private LocalDate billActivityDate(Bill bill) {
        LocalDate paid = dateOfDateTime(bill.getPaidAt());
        return paid != null ? paid : parseDate(bill.getCreatedDate());
    }

    private LocalDate refundActivityDate(RefundRequest refund) {
        LocalDate processed = dateOfDateTime(refund.getProcessedAt());
        return processed != null ? processed : dateOfDateTime(refund.getCreatedAt());
    }

    private boolean inRange(LocalDate value, LocalDate start, LocalDate end) {
        return value != null && !value.isBefore(start) && !value.isAfter(end);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value.trim()); }
        catch (DateTimeParseException e) {
            LocalDateTime dt = parseDateTime(value);
            return dt != null ? dt.toLocalDate() : null;
        }
    }

    private LocalDate dateOfDateTime(String value) {
        LocalDateTime dateTime = parseDateTime(value);
        if (dateTime != null) return dateTime.toLocalDate();
        return parseDate(value);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDateTime.parse(value.trim()); }
        catch (DateTimeParseException e) { return null; }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        // Prevent spreadsheet formula execution if a user-controlled value is opened in Excel/Sheets.
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        safe = safe.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
