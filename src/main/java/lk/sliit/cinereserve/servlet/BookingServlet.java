package lk.sliit.cinereserve.servlet;

import lk.sliit.cinereserve.dao.BookingDAO;
import lk.sliit.cinereserve.model.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BookingServlet – REST Controller for Booking management.
 *
 * Endpoints:
 *   POST   /api/bookings              – Create (reserve seats)
 *   GET    /api/bookings              – All bookings (admin)
 *   GET    /api/bookings/user/{uid}   – User's own bookings
 *   GET    /api/bookings/{id}         – Single booking
 *   PUT    /api/bookings/{id}/cancel  – Cancel booking
 *   GET    /api/bookings/stats        – Revenue & count stats
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    // ── CREATE: Reserve seats ────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            int          userId     = Integer.parseInt(body.get("userId").toString());
            String       userName   = body.get("userName").toString();
            int          movieId    = Integer.parseInt(body.get("movieId").toString());
            String       movieTitle = body.get("movieTitle").toString();
            int          showtimeId = Integer.parseInt(body.get("showtimeId").toString());
            @SuppressWarnings("unchecked")
            List<String> seats      = (List<String>) body.get("seats");

            if (seats == null || seats.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Select at least one seat."));
            }

            Booking booking = bookingDAO.create(userId, userName, movieId, movieTitle, showtimeId, seats);
            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "One or more seats are already taken."));
            }
            return ResponseEntity.ok(Map.of("success", true, "bookingId", booking.getId(), "total", booking.getTotal()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    // ── READ: All bookings (admin view) ─────────────────────────
    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingDAO.findAll());
    }

    // ── READ: User's bookings ────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(bookingDAO.findByUserId(userId));
    }

    // ── READ: Single booking ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Booking b = bookingDAO.findById(id);
        if (b == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(b);
    }

    // ── UPDATE: Cancel booking ───────────────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        boolean ok = bookingDAO.cancel(id);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "Booking not found or already cancelled."));
        return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled and seats released."));
    }

    // ── Stats (admin dashboard) ──────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(Map.of(
            "activeBookings", bookingDAO.countActive(),
            "totalRevenue",   bookingDAO.totalRevenue()
        ));
    }
}
