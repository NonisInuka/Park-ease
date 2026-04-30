package lk.sliit.cinereserve.dao;

import lk.sliit.cinereserve.model.Booking;
import lk.sliit.cinereserve.model.Showtime;
import lk.sliit.cinereserve.util.FileHandler;
import lk.sliit.cinereserve.util.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingDAO – CRUD for Booking records (bookings.txt).
 */
public class BookingDAO {

    private final FileHandler   fileHandler   = new FileHandler("bookings.txt");
    private final ShowtimeDAO   showtimeDAO   = new ShowtimeDAO();

    // ── CREATE ───────────────────────────────────────────────────
    /**
     * Creates a new booking, reserves the seats, and writes to bookings.txt.
     * Returns null if any seat is already taken.
     */
    public Booking create(int userId, String userName,
                          int movieId, String movieTitle,
                          int showtimeId, List<String> seats) {
        // Validate seats are still available
        Showtime showtime = showtimeDAO.findById(showtimeId);
        if (showtime == null) return null;
        for (String seat : seats) {
            if (showtime.isSeatTaken(seat)) return null; // seat conflict
        }

        String id      = IdGenerator.nextBookingId();
        String now     = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Booking booking = new Booking(
            id, userId, userName, movieId, movieTitle,
            showtimeId, showtime.getDate(), showtime.getTime(),
            showtime.getHall(), seats, now
        );
        fileHandler.appendLine(booking.toCsv());

        // Reserve seats in showtime record
        showtimeDAO.reserveSeats(showtimeId, seats);
        return booking;
    }

    // ── READ ─────────────────────────────────────────────────────
    public List<Booking> findAll() {
        List<Booking> list  = new ArrayList<>();
        List<String>  lines = fileHandler.readAll();
        for (String line : lines) {
            Booking b = Booking.fromCsv(line);
            if (b != null) list.add(b);
        }
        return list;
    }

    public Booking findById(String id) {
        for (Booking b : findAll()) {
            if (b.getId().equals(id)) return b;
        }
        return null;
    }

    public List<Booking> findByUserId(int userId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : findAll()) {
            if (b.getUserId() == userId) result.add(b);
        }
        return result;
    }

    // ── UPDATE ───────────────────────────────────────────────────
    public boolean update(Booking booking) {
        if (findById(booking.getId()) == null) return false;
        fileHandler.updateById(booking.getId(), booking.toCsv());
        return true;
    }

    // ── DELETE / CANCEL ──────────────────────────────────────────
    /**
     * Cancels a booking: sets status to "cancelled" and releases seats.
     */
    public boolean cancel(String bookingId) {
        Booking booking = findById(bookingId);
        if (booking == null || booking.isCancelled()) return false;
        booking.cancel();
        fileHandler.updateById(bookingId, booking.toCsv());
        showtimeDAO.releaseSeats(booking.getShowtimeId(), booking.getSeats());
        return true;
    }

    // ── STATS ────────────────────────────────────────────────────
    public int countActive() {
        return (int) findAll().stream()
                .filter(b -> !b.isCancelled()).count();
    }

    public int totalRevenue() {
        return findAll().stream()
                .filter(b -> Booking.STATUS_CONFIRMED.equals(b.getStatus()))
                .mapToInt(Booking::getTotal).sum();
    }
}
