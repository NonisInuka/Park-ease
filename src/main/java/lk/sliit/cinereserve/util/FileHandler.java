package lk.sliit.cinereserve.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler – Generic file read/write utility for data persistence.
 *
 * Replaces a database: all records are stored as pipe-delimited
 * text files in src/main/resources/data/.
 *
 * OOP Concepts demonstrated:
 *   - Encapsulation : private file path field + public methods
 *   - Abstraction   : callers don't need to know HOW files are opened
 *
 * File locations (resolved from classpath or working directory):
 *   data/movies.txt
 *   data/users.txt
 *   data/bookings.txt
 *   data/showtimes.txt
 *
 * Usage example:
 *   FileHandler fh = new FileHandler("data/movies.txt");
 *   List<String> lines = fh.readAll();
 *   fh.appendLine("7|Inception|Thriller|148|8.8|🎬|#1a1a2a");
 */
public class FileHandler {

    private static final String DATA_DIR = "src/main/resources/data/";

    private final String filePath;

    // ── Constructor ──────────────────────────────────────────────
    public FileHandler(String fileName) {
        this.filePath = DATA_DIR + fileName;
        ensureFileExists();
    }

    // ── Private Helpers ──────────────────────────────────────────
    private void ensureFileExists() {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Could not create " + filePath + ": " + e.getMessage());
        }
    }

    // ── Public API ───────────────────────────────────────────────

    /**
     * READ – Returns all non-empty, non-comment lines from the file.
     * (Comment lines start with '#' and are used for headers.)
     */
    public List<String> readAll() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Read error on " + filePath + ": " + e.getMessage());
        }
        return lines;
    }

    /**
     * WRITE – Overwrites the entire file with the given lines.
     * Used for UPDATE and DELETE (rewrite all except deleted record).
     */
    public void writeAll(List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Write error on " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * CREATE – Appends a single line to the file.
     * Used for INSERT (new record).
     */
    public void appendLine(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[FileHandler] Append error on " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * DELETE – Removes all lines that start with the given id prefix.
     * e.g. deleteById("3|") removes the line for movie with id=3.
     */
    public void deleteById(String idPrefix) {
        List<String> lines  = readAll();
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith(idPrefix + "|") && !line.equals(idPrefix)) {
                result.add(line);
            }
        }
        writeAll(result);
    }

    /**
     * UPDATE – Replaces the line that starts with the id prefix.
     */
    public void updateById(String idPrefix, String newLine) {
        List<String> lines  = readAll();
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith(idPrefix + "|")) {
                result.add(newLine);
            } else {
                result.add(line);
            }
        }
        writeAll(result);
    }

    /**
     * Reads the last integer ID from the file so new records get id+1.
     * Returns 0 if the file is empty.
     */
    public int getLastId() {
        List<String> lines = readAll();
        if (lines.isEmpty()) return 0;
        String lastLine = lines.get(lines.size() - 1);
        try {
            return Integer.parseInt(lastLine.split("\\|")[0].trim());
        } catch (NumberFormatException e) {
            return lines.size();
        }
    }

    /**
     * Returns the number of records in the file.
     */
    public int count() {
        return readAll().size();
    }

    public String getFilePath() { return filePath; }
}
