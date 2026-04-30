package lk.sliit.cinereserve.model;

/**
 * User – Base model class demonstrating ENCAPSULATION.
 *
 * All fields are private; access is controlled via
 * public getters and setters (JavaBean standard).
 *
 * OOP Concepts demonstrated:
 *   - Encapsulation  : private fields + public accessors
 *   - Inheritance    : AdminUser extends this class
 *   - Polymorphism   : getRole() overridden in AdminUser
 */
public class User {

    // ── Private fields (Encapsulation) ──────────────────────────
    private int    id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;   // stored as plain text for demo; hash in production
    private String role;       // "customer" | "admin"
    private String joinedDate; // ISO date string e.g. "2025-07-12"

    // ── Constructors ─────────────────────────────────────────────
    public User() {}

    public User(int id, String firstName, String lastName,
                String email, String password, String joinedDate) {
        this.id         = id;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.password   = password;
        this.role       = "customer";
        this.joinedDate = joinedDate;
    }

    // ── Getters & Setters (Encapsulation) ────────────────────────
    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getFirstName()                    { return firstName; }
    public void   setFirstName(String firstName)    { this.firstName = firstName; }

    public String getLastName()                     { return lastName; }
    public void   setLastName(String lastName)      { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getEmail()                  { return email; }
    public void   setEmail(String email)      { this.email = email; }

    public String getPassword()                   { return password; }
    public void   setPassword(String password)    { this.password = password; }

    public String getJoinedDate()                     { return joinedDate; }
    public void   setJoinedDate(String joinedDate)    { this.joinedDate = joinedDate; }

    // Polymorphism: overridden in AdminUser
    public String getRole() { return role; }
    public void   setRole(String role) { this.role = role; }

    /**
     * Returns a CSV line for file storage.
     * Format: id,firstName,lastName,email,password,role,joinedDate
     */
    public String toCsv() {
        return id + "," + firstName + "," + lastName + ","
             + email + "," + password + "," + getRole() + "," + joinedDate;
    }

    /**
     * Creates a User from a CSV line (file read operation).
     */
    public static User fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 7) return null;
        String role = p[5].trim();
        User u;
        if ("admin".equalsIgnoreCase(role)) {
            u = new AdminUser(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                              p[3].trim(), p[4].trim(), p[6].trim());
        } else {
            u = new User(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                         p[3].trim(), p[4].trim(), p[6].trim());
        }
        return u;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + getFullName() + ", role=" + getRole() + "}";
    }
}
