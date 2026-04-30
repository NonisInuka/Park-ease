package lk.sliit.cinereserve.model;

/**
 * AdminUser – extends User, demonstrating INHERITANCE and POLYMORPHISM.
 *
 * OOP Concepts demonstrated:
 *   - Inheritance   : AdminUser IS-A User (extends User)
 *   - Polymorphism  : getRole() overrides parent implementation
 *                     getDetails() provides richer admin info
 */
public class AdminUser extends User {

    private String department;  // Extra field only admins have
    private int    accessLevel; // 1 = read-only, 2 = full access

    // ── Constructors ─────────────────────────────────────────────
    public AdminUser() {
        super();
        this.department  = "General";
        this.accessLevel = 2;
    }

    public AdminUser(int id, String firstName, String lastName,
                     String email, String password, String joinedDate) {
        super(id, firstName, lastName, email, password, joinedDate);
        this.department  = "Cinema Management";
        this.accessLevel = 2;
    }

    // ── Polymorphism: Override getRole() ─────────────────────────
    @Override
    public String getRole() {
        return "admin";
    }

    // ── Admin-specific methods ───────────────────────────────────
    public String getDepartment()                     { return department; }
    public void   setDepartment(String department)    { this.department = department; }

    public int    getAccessLevel()                    { return accessLevel; }
    public void   setAccessLevel(int accessLevel)     { this.accessLevel = accessLevel; }

    /**
     * Polymorphic method – richer output than User.toString()
     */
    public String getDetails() {
        return "AdminUser{name=" + getFullName()
             + ", dept=" + department
             + ", accessLevel=" + accessLevel + "}";
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
