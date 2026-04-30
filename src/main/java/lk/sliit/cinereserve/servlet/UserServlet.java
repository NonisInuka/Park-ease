package lk.sliit.cinereserve.servlet;

import lk.sliit.cinereserve.dao.UserDAO;
import lk.sliit.cinereserve.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserServlet (Spring REST Controller) – Handles all User operations.
 *
 * Endpoints:
 *   POST   /api/users/register         – Create new user account
 *   POST   /api/users/login            – Authenticate user
 *   GET    /api/users                  – List all users (admin)
 *   GET    /api/users/{id}             – Get user by ID
 *   PUT    /api/users/{id}             – Update user details
 *   DELETE /api/users/{id}             – Delete user account
 *
 * Note: In a full JSP/Servlet project (non-Spring), extend HttpServlet
 * and override doGet/doPost instead. Spring Boot is used here for
 * easier setup; the DAO and model classes are the same.
 */
@RestController
@RequestMapping("/api/users")
public class UserServlet {

    private final UserDAO userDAO = new UserDAO();

    // ── CREATE: Register ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");
        String email     = body.get("email");
        String password  = body.get("password");

        if (firstName == null || lastName == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required."));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }

        User user = userDAO.register(firstName, lastName, email, password);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered."));
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Registration successful.",
            "userId",  user.getId(),
            "name",    user.getFullName(),
            "role",    user.getRole()
        ));
    }

    // ── READ: Login ──────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password required."));
        }

        User user = userDAO.login(email, password);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password."));
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "userId",  user.getId(),
            "name",    user.getFullName(),
            "email",   user.getEmail(),
            "role",    user.getRole()
        ));
    }

    // ── READ: Get all users ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userDAO.findAll());
    }

    // ── READ: Get user by ID ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        User user = userDAO.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    // ── UPDATE: Update user details ──────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id,
                                    @RequestBody Map<String, String> body) {
        User user = userDAO.findById(id);
        if (user == null) return ResponseEntity.notFound().build();

        if (body.containsKey("firstName")) user.setFirstName(body.get("firstName"));
        if (body.containsKey("lastName"))  user.setLastName(body.get("lastName"));
        if (body.containsKey("email"))     user.setEmail(body.get("email"));
        if (body.containsKey("password"))  user.setPassword(body.get("password"));

        boolean ok = userDAO.update(user);
        if (!ok) return ResponseEntity.internalServerError().body(Map.of("error", "Update failed."));
        return ResponseEntity.ok(Map.of("success", true, "message", "User updated."));
    }

    // ── DELETE: Remove user ──────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        boolean ok = userDAO.delete(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted."));
    }
}
