package lk.sliit.cinereserve.servlet;

import lk.sliit.cinereserve.dao.MovieDAO;
import lk.sliit.cinereserve.model.Movie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MovieServlet – REST Controller for Movie management.
 *
 * Endpoints:
 *   GET    /api/movies           – List all movies
 *   GET    /api/movies/{id}      – Get movie by ID
 *   GET    /api/movies?genre=X   – Filter by genre
 *   POST   /api/movies           – Add new movie (admin)
 *   PUT    /api/movies/{id}      – Update movie (admin)
 *   DELETE /api/movies/{id}      – Delete movie (admin)
 */
@RestController
@RequestMapping("/api/movies")
public class MovieServlet {

    private final MovieDAO movieDAO = new MovieDAO();

    // ── READ: All movies (optionally filtered by genre) ──────────
    @GetMapping
    public ResponseEntity<List<Movie>> getAll(
            @RequestParam(required = false) String genre) {
        List<Movie> movies = (genre != null && !genre.isBlank())
            ? movieDAO.findByGenre(genre)
            : movieDAO.findAll();
        return ResponseEntity.ok(movies);
    }

    // ── READ: Single movie ───────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Movie m = movieDAO.findById(id);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }

    // ── CREATE: Add movie ────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String title  = body.get("title");
        String genre  = body.get("genre");
        String durStr = body.get("duration");
        String ratStr = body.get("rating");

        if (title == null || genre == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and genre are required."));
        }
        int    duration = durStr != null ? Integer.parseInt(durStr) : 120;
        double rating   = ratStr != null ? Double.parseDouble(ratStr) : 7.0;

        Movie m = movieDAO.create(title, genre, duration, rating);
        return ResponseEntity.ok(Map.of("success", true, "movie", m));
    }

    // ── UPDATE: Modify movie ─────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id,
                                    @RequestBody Map<String, String> body) {
        Movie m = movieDAO.findById(id);
        if (m == null) return ResponseEntity.notFound().build();

        if (body.containsKey("title"))    m.setTitle(body.get("title"));
        if (body.containsKey("genre"))    m.setGenre(body.get("genre"));
        if (body.containsKey("duration")) m.setDuration(Integer.parseInt(body.get("duration")));
        if (body.containsKey("rating"))   m.setRating(Double.parseDouble(body.get("rating")));

        boolean ok = movieDAO.update(m);
        return ok ? ResponseEntity.ok(Map.of("success", true))
                  : ResponseEntity.internalServerError().build();
    }

    // ── DELETE: Remove movie ─────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        boolean ok = movieDAO.delete(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true, "message", "Movie deleted."));
    }
}
