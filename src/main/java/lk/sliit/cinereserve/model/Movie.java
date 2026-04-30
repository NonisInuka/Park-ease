package lk.sliit.cinereserve.model;

/**
 * Movie – Model class with full encapsulation.
 *
 * Data is serialized to/from CSV for file-based storage.
 * Format: id,title,genre,duration,rating,emoji,color
 */
public class Movie {

    private int    id;
    private String title;
    private String genre;
    private int    duration; // minutes
    private double rating;   // 1.0 – 10.0
    private String emoji;    // poster emoji e.g. "🚀"
    private String color;    // hex background e.g. "#1a2a4a"

    // ── Constructors ─────────────────────────────────────────────
    public Movie() {}

    public Movie(int id, String title, String genre,
                 int duration, double rating, String emoji, String color) {
        this.id       = id;
        this.title    = title;
        this.genre    = genre;
        this.duration = duration;
        this.rating   = rating;
        this.emoji    = emoji;
        this.color    = color;
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public String getTitle()                { return title; }
    public void   setTitle(String title)    { this.title = title; }

    public String getGenre()                { return genre; }
    public void   setGenre(String genre)    { this.genre = genre; }

    public int    getDuration()                 { return duration; }
    public void   setDuration(int duration)     { this.duration = duration; }

    public double getRating()                   { return rating; }
    public void   setRating(double rating)      { this.rating = rating; }

    public String getEmoji()                { return emoji; }
    public void   setEmoji(String emoji)    { this.emoji = emoji; }

    public String getColor()                { return color; }
    public void   setColor(String color)    { this.color = color; }

    // ── File Serialization ───────────────────────────────────────
    /**
     * Converts this Movie to a CSV line for writing to movies.txt
     */
    public String toCsv() {
        return id + "|" + title + "|" + genre + "|"
             + duration + "|" + rating + "|" + emoji + "|" + color;
    }

    /**
     * Reconstructs a Movie from a CSV line read from movies.txt
     */
    public static Movie fromCsv(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 7) return null;
        return new Movie(
            Integer.parseInt(p[0].trim()),
            p[1].trim(), p[2].trim(),
            Integer.parseInt(p[3].trim()),
            Double.parseDouble(p[4].trim()),
            p[5].trim(), p[6].trim()
        );
    }

    @Override
    public String toString() {
        return "Movie{id=" + id + ", title='" + title + "', genre=" + genre
             + ", duration=" + duration + "min, rating=" + rating + "}";
    }
}
