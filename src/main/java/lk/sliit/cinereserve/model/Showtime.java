package lk.sliit.cinereserve.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Showtime – Represents a single screening of a movie.
 *
 * Stores taken seat IDs so the seat map can show availability.
 * CSV format (pipe-delimited):
 *   id|movieId|date|time|hall|seat1,seat2,...
 */
public class Showtime {

    private int          id;
    private int          movieId;
    private String       date;       // e.g. "2025-07-12"
    private String       time;       // e.g. "19:30"
    private String       hall;       // e.g. "Hall A"
    private List<String> takenSeats; // e.g. ["A1","B3","C2"]

    // Total capacity: 5 rows × 8 cols = 40 seats
    public static final int TOTAL_SEATS = 40;

    // ── Constructors ─────────────────────────────────────────────
    public Showtime() {
        this.takenSeats = new ArrayList<>();
    }

    public Showtime(int id, int movieId, String date, String time, String hall) {
        this.id         = id;
        this.movieId    = movieId;
        this.date       = date;
        this.time       = time;
        this.hall       = hall;
        this.takenSeats = new ArrayList<>();
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public int    getMovieId()                  { return movieId; }
    public void   setMovieId(int movieId)       { this.movieId = movieId; }

    public String getDate()                 { return date; }
    public void   setDate(String date)      { this.date = date; }

    public String getTime()                 { return time; }
    public void   setTime(String time)      { this.time = time; }

    public String getHall()                 { return hall; }
    public void   setHall(String hall)      { this.hall = hall; }

    public List<String> getTakenSeats()                          { return takenSeats; }
    public void         setTakenSeats(List<String> takenSeats)   { this.takenSeats = takenSeats; }

    // ── Business Logic ───────────────────────────────────────────
    public int getAvailableSeats() {
        return TOTAL_SEATS - takenSeats.size();
    }

    public boolean isSeatTaken(String seatId) {
        return takenSeats.contains(seatId);
    }

    public boolean isFull() {
        return takenSeats.size() >= TOTAL_SEATS;
    }

    public void reserveSeat(String seatId) {
        if (!takenSeats.contains(seatId)) takenSeats.add(seatId);
    }

    public void releaseSeat(String seatId) {
        takenSeats.remove(seatId);
    }

    // ── File Serialization ───────────────────────────────────────
    public String toCsv() {
        String seats = takenSeats.isEmpty() ? "" : String.join(",", takenSeats);
        return id + "|" + movieId + "|" + date + "|" + time + "|" + hall + "|" + seats;
    }

    public static Showtime fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 5) return null;
        Showtime s = new Showtime(
            Integer.parseInt(p[0].trim()),
            Integer.parseInt(p[1].trim()),
            p[2].trim(), p[3].trim(), p[4].trim()
        );
        if (p.length > 5 && !p[5].trim().isEmpty()) {
            s.setTakenSeats(new ArrayList<>(Arrays.asList(p[5].trim().split(","))));
        }
        return s;
    }

    @Override
    public String toString() {
        return "Showtime{id=" + id + ", movieId=" + movieId + ", date=" + date
             + ", time=" + time + ", hall=" + hall
             + ", available=" + getAvailableSeats() + "}";
    }
}
