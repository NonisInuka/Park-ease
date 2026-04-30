package lk.sliit.cinereserve.dao;

import lk.sliit.cinereserve.model.Showtime;
import lk.sliit.cinereserve.util.FileHandler;
import lk.sliit.cinereserve.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * ShowtimeDAO – CRUD for Showtime records (showtimes.txt).
 */
public class ShowtimeDAO {

    private final FileHandler fileHandler = new FileHandler("showtimes.txt");

    // ── CREATE ───────────────────────────────────────────────────
    public Showtime create(int movieId, String date, String time, String hall) {
        int id = IdGenerator.nextIntId(fileHandler);
        Showtime s = new Showtime(id, movieId, date, time, hall);
        fileHandler.appendLine(s.toCsv());
        return s;
    }

    // ── READ ─────────────────────────────────────────────────────
    public List<Showtime> findAll() {
        List<Showtime> list  = new ArrayList<>();
        List<String>   lines = fileHandler.readAll();
        for (String line : lines) {
            Showtime s = Showtime.fromCsv(line);
            if (s != null) list.add(s);
        }
        return list;
    }

    public Showtime findById(int id) {
        for (Showtime s : findAll()) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public List<Showtime> findByMovieId(int movieId) {
        List<Showtime> result = new ArrayList<>();
        for (Showtime s : findAll()) {
            if (s.getMovieId() == movieId) result.add(s);
        }
        return result;
    }

    // ── UPDATE ───────────────────────────────────────────────────
    public boolean update(Showtime showtime) {
        if (findById(showtime.getId()) == null) return false;
        fileHandler.updateById(String.valueOf(showtime.getId()), showtime.toCsv());
        return true;
    }

    // Mark seats as taken (called after a booking is confirmed)
    public boolean reserveSeats(int showtimeId, List<String> seatIds) {
        Showtime s = findById(showtimeId);
        if (s == null) return false;
        seatIds.forEach(s::reserveSeat);
        return update(s);
    }

    // Release seats (called when a booking is cancelled)
    public boolean releaseSeats(int showtimeId, List<String> seatIds) {
        Showtime s = findById(showtimeId);
        if (s == null) return false;
        seatIds.forEach(s::releaseSeat);
        return update(s);
    }

    // ── DELETE ───────────────────────────────────────────────────
    public boolean delete(int id) {
        if (findById(id) == null) return false;
        fileHandler.deleteById(String.valueOf(id));
        return true;
    }
}
