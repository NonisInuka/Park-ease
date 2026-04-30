package lk.sliit.cinereserve.servlet;

import lk.sliit.cinereserve.dao.ShowtimeDAO;
import lk.sliit.cinereserve.model.Showtime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ShowtimeServlet – REST Controller for Showtime scheduling.
 *
 * Endpoints:
 *   GET    /api/showtimes               – All showtimes
 *   GET    /api/showtimes?movieId=X     – Showtimes for a movie
 *   GET    /api/showtimes/{id}          – Single showtime + seat map
 *   POST   /api/showtimes               – Schedule new showtime (admin)
 *   DELETE /api/showtimes/{id}          – Remove showtime (admin)
 */
@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeServlet {

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    // ── READ: All / by movie ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Showtime>> getAll(
            @RequestParam(required = false) Integer movieId) {
        List<Showtime> result = (movieId != null)
            ? showtimeDAO.findByMovieId(movieId)
            : showtimeDAO.findAll();
        return ResponseEntity.ok(result);
    }

    // ── READ: Single showtime (includes taken seats) ─────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Showtime s = showtimeDAO.findById(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    // ── CREATE: Schedule showtime ────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String movieIdStr = body.get("movieId");
        String date       = body.get("date");
        String time       = body.get("time");
        String hall       = body.get("hall");

        if (movieIdStr == null || date == null || time == null || hall == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "movieId, date, time, hall required."));
        }
        Showtime s = showtimeDAO.create(Integer.parseInt(movieIdStr), date, time, hall);
        return ResponseEntity.ok(Map.of("success", true, "showtime", s));
    }

    // ── DELETE: Remove showtime ──────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        boolean ok = showtimeDAO.delete(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
