package lk.sliit.cinereserve.dao;

import lk.sliit.cinereserve.model.User;
import lk.sliit.cinereserve.util.FileHandler;
import lk.sliit.cinereserve.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO – Data Access Object for User CRUD operations.
 *
 * All reads and writes go through FileHandler → users.txt
 *
 * CRUD operations:
 *   CREATE – register(...)
 *   READ   – findAll(), findById(), findByEmail()
 *   UPDATE – update(...)
 *   DELETE – delete(...)
 */
public class UserDAO {

    private final FileHandler fileHandler = new FileHandler("users.txt");

    // ── CREATE ───────────────────────────────────────────────────
    /**
     * Registers a new user (writes a new line to users.txt).
     * Returns the saved User with its assigned ID, or null if email taken.
     */
    public User register(String firstName, String lastName,
                         String email, String password) {
        // Check email uniqueness (Read before Create)
        if (findByEmail(email) != null) return null;

        int id = IdGenerator.nextIntId(fileHandler);
        String today = LocalDate.now().toString();
        User user = new User(id, firstName, lastName, email, password, today);
        fileHandler.appendLine(user.toCsv());
        return user;
    }

    // ── READ ─────────────────────────────────────────────────────
    /**
     * Returns all users from users.txt.
     */
    public List<User> findAll() {
        List<User>   users = new ArrayList<>();
        List<String> lines = fileHandler.readAll();
        for (String line : lines) {
            User u = User.fromCsv(line);
            if (u != null) users.add(u);
        }
        return users;
    }

    /**
     * Finds a single user by numeric ID.
     */
    public User findById(int id) {
        for (User u : findAll()) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    /**
     * Finds a user by email address (used for login).
     */
    public User findByEmail(String email) {
        for (User u : findAll()) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    /**
     * Validates login credentials.
     * Returns the User if credentials are correct, otherwise null.
     */
    public User login(String email, String password) {
        User user = findByEmail(email);
        if (user != null && user.getPassword().equals(password)) return user;
        return null;
    }

    // ── UPDATE ───────────────────────────────────────────────────
    /**
     * Updates an existing user's details (overwrites their CSV line).
     */
    public boolean update(User updatedUser) {
        User existing = findById(updatedUser.getId());
        if (existing == null) return false;
        fileHandler.updateById(String.valueOf(updatedUser.getId()), updatedUser.toCsv());
        return true;
    }

    // ── DELETE ───────────────────────────────────────────────────
    /**
     * Deletes a user by ID (removes their line from users.txt).
     */
    public boolean delete(int id) {
        User existing = findById(id);
        if (existing == null) return false;
        fileHandler.deleteById(String.valueOf(id));
        return true;
    }

    /**
     * Returns the total count of registered users.
     */
    public int count() {
        return findAll().size();
    }
}
