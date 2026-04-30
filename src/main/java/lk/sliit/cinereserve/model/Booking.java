package lk.sliit.cinereserve.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Booking – Represents a confirmed seat reservation.
 *
 * CSV format (pipe-delimited):
 *   id|userId|userName|movieId|movieTitle|showtimeId|date|time|hall|seats|total|status|bookedAt
 */
public class Booking {

    // ── Status constants (replaces magic strings) ────────────────
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_PENDING   = "pending";
    public static final String STATUS_CANCELLED = "cancelled";

    // Pricing (LKR)
    public static final int PRICE_STANDARD = 700;
    public static final int PRICE_VIP      = 1400;

    // VIP rows (D and E)
    private static final List<String> VIP_ROWS = Arrays.asList("D", "E");

    private String       id;         // e.g. "BK0001"
    private int          userId;
    private String       userName;
    private int          movieId;
    private String       movieTitle;
    private int          showtimeId;
    private String       date;
    private String       time;
    private String       hall;
    private List<String> seats;
    private int          total;      // LKR
    private String       status;
    private String       bookedAt;   // ISO datetime

    // ── Constructors ─────────────────────────────────────────────
    public Booking() {
        this.seats  = new ArrayList<>();
        this.status = STATUS_CONFIRMED;
    }

    public Booking(String id, int userId, String userName,
                   int movieId, String movieTitle, int showtimeId,
                   String date, String time, String hall,
                   List<String> seats, String bookedAt) {
        this.id          = id;
        this.userId      = userId;
        this.userName    = userName;
        this.movieId     = movieId;
        this.movieTitle  = movieTitle;
        this.showtimeId  = showtimeId;
        this.date        = date;
        this.time        = time;
        this.hall        = hall;
        this.seats       = seats != null ? seats : new ArrayList<>();
        this.status      = STATUS_CONFIRMED;
        this.bookedAt    = bookedAt;
        this.total       = calculateTotal(this.seats);
    }

    // ── Business Logic ───────────────────────────────────────────
    /**
     * Calculates total price based on seat type (VIP vs standard).
     */
    public static int calculateTotal(List<String> seats) {
        int total = 0;
        for (String seat : seats) {
            String row = String.valueOf(seat.charAt(0));
            total += VIP_ROWS.contains(row) ? PRICE_VIP : PRICE_STANDARD;
        }
        return total;
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(this.status);
    }

    // ── Getters & Setters ────────────────────────────────────────
    public String       getId()                         { return id; }
    public void         setId(String id)                { this.id = id; }

    public int          getUserId()                     { return userId; }
    public void         setUserId(int userId)           { this.userId = userId; }

    public String       getUserName()                   { return userName; }
    public void         setUserName(String userName)    { this.userName = userName; }

    public int          getMovieId()                    { return movieId; }
    public void         setMovieId(int movieId)         { this.movieId = movieId; }

    public String       getMovieTitle()                         { return movieTitle; }
    public void         setMovieTitle(String movieTitle)        { this.movieTitle = movieTitle; }

    public int          getShowtimeId()                         { return showtimeId; }
    public void         setShowtimeId(int showtimeId)           { this.showtimeId = showtimeId; }

    public String       getDate()                   { return date; }
    public void         setDate(String date)        { this.date = date; }

    public String       getTime()                   { return time; }
    public void         setTime(String time)        { this.time = time; }

    public String       getHall()                   { return hall; }
    public void         setHall(String hall)        { this.hall = hall; }

    public List<String> getSeats()                          { return seats; }
    public void         setSeats(List<String> seats)        { this.seats = seats; }

    public int          getTotal()                  { return total; }
    public void         setTotal(int total)         { this.total = total; }

    public String       getStatus()                     { return status; }
    public void         setStatus(String status)        { this.status = status; }

    public String       getBookedAt()                       { return bookedAt; }
    public void         setBookedAt(String bookedAt)        { this.bookedAt = bookedAt; }

    // ── File Serialization ───────────────────────────────────────
    public String toCsv() {
        return id + "|" + userId + "|" + userName + "|"
             + movieId + "|" + movieTitle + "|" + showtimeId + "|"
             + date + "|" + time + "|" + hall + "|"
             + String.join(",", seats) + "|"
             + total + "|" + status + "|" + bookedAt;
    }

    public static Booking fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 13) return null;
        Booking b = new Booking();
        b.setId(p[0].trim());
        b.setUserId(Integer.parseInt(p[1].trim()));
        b.setUserName(p[2].trim());
        b.setMovieId(Integer.parseInt(p[3].trim()));
        b.setMovieTitle(p[4].trim());
        b.setShowtimeId(Integer.parseInt(p[5].trim()));
        b.setDate(p[6].trim());
        b.setTime(p[7].trim());
        b.setHall(p[8].trim());
        b.setSeats(new ArrayList<>(Arrays.asList(p[9].trim().split(","))));
        b.setTotal(Integer.parseInt(p[10].trim()));
        b.setStatus(p[11].trim());
        b.setBookedAt(p[12].trim());
        return b;
    }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", user=" + userName
             + ", movie=" + movieTitle + ", seats=" + seats
             + ", total=LKR" + total + ", status=" + status + "}";
    }
}
