package lk.sliit.cinereserve.dao;

import lk.sliit.cinereserve.model.Movie;
import lk.sliit.cinereserve.util.FileHandler;
import lk.sliit.cinereserve.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieDAO – Data Access Object for Movie CRUD operations.
 * All data persisted to/from movies.txt via FileHandler.
 */
public class MovieDAO {

    private final FileHandler fileHandler = new FileHandler("movies.txt");

    // Emoji and color defaults by genre
    private static String emojiForGenre(String genre) {
        switch (genre) {
            case "Action":   return "💥";
            case "Thriller": return "🔪";
            case "Drama":    return "🎭";
            case "Sci-Fi":   return "🚀";
            case "Comedy":   return "😄";
            default:         return "🎬";
        }
    }
    private static String colorForGenre(String genre) {
        switch (genre) {
            case "Action":   return "#2a1f1a";
            case "Thriller": return "#2a1a1a";
            case "Drama":    return "#1a2a2a";
            case "Sci-Fi":   return "#1a2a4a";
            case "Comedy":   return "#1a2a1a";
            default:         return "#1e1e2e";
        }
    }

    // ── CREATE ───────────────────────────────────────────────────
    public Movie create(String title, String genre, int duration, double rating) {
        int id = IdGenerator.nextIntId(fileHandler);
        Movie m = new Movie(id, title, genre, duration, rating,
                            emojiForGenre(genre), colorForGenre(genre));
        fileHandler.appendLine(m.toCsv());
        return m;
    }

    // ── READ ─────────────────────────────────────────────────────
    public List<Movie> findAll() {
        List<Movie>  movies = new ArrayList<>();
        List<String> lines  = fileHandler.readAll();
        for (String line : lines) {
            Movie m = Movie.fromCsv(line);
            if (m != null) movies.add(m);
        }
        return movies;
    }

    public Movie findById(int id) {
        for (Movie m : findAll()) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public List<Movie> findByGenre(String genre) {
        List<Movie> result = new ArrayList<>();
        for (Movie m : findAll()) {
            if (m.getGenre().equalsIgnoreCase(genre)) result.add(m);
        }
        return result;
    }

    // ── UPDATE ───────────────────────────────────────────────────
    public boolean update(Movie updated) {
        if (findById(updated.getId()) == null) return false;
        fileHandler.updateById(String.valueOf(updated.getId()), updated.toCsv());
        return true;
    }

    // ── DELETE ───────────────────────────────────────────────────
    public boolean delete(int id) {
        if (findById(id) == null) return false;
        fileHandler.deleteById(String.valueOf(id));
        return true;
    }

    public int count() { return findAll().size(); }
}
