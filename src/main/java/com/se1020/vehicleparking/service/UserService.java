package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.UserRoles;
import com.se1020.vehicleparking.pattern.user.UserFactory;
import com.se1020.vehicleparking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserFactory userFactory;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Validation helper methods. These mirror the HTML5 rules on the forms so that the server
    // enforces the same constraints even if the browser-side checks are bypassed.
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^[0-9]{10,15}$");

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String e = email.trim();
        return e.length() <= 254 && EMAIL_PATTERN.matcher(e).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) return false;
        return password.length() >= 6; // Keep 6 character minimum
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    private boolean isValidName(String name) {
        if (name == null) return false;
        int len = name.trim().length();
        return len >= 2 && len <= 100;
    }

    private boolean isValidLicenseNumber(String license) {
        if (license == null) return false;
        int len = license.trim().length();
        return len >= 5 && len <= 30;
    }

    private boolean isValidAdminCode(String adminCode) {
        if (adminCode == null || adminCode.trim().isEmpty()) return false;
        return true; // Just check if not empty
    }

    /** True when another account (different userId) already uses this email (case-insensitive). */
    private boolean isEmailTaken(String email, String excludeUserId) {
        User existing = userRepository.findByEmail(email.trim());
        return existing != null && (excludeUserId == null || !excludeUserId.equals(existing.getUserId()));
    }

    /** Validates the fields shared by every account type. Returns an error message or null. */
    private String validateCommonFields(String name, String email, String password, String phone) {
        if (!isValidName(name)) return "Full name must be between 2 and 100 characters.";
        if (!isValidEmail(email)) return "Please enter a valid email address (e.g. john@example.com).";
        if (!isValidPassword(password)) return "Password must be at least 6 characters long.";
        if (!isValidPhone(phone)) return "Phone number must contain 10 to 15 digits only.";
        if (isEmailTaken(email, null)) return "An account with this email address already exists.";
        return null;
    }

    /**
     * Detects whether a stored password is already a BCrypt hash (starts with the standard
     * $2a$/$2b$/$2y$ prefix). Any seeded/legacy plaintext password will not match this and is
     * handled by the lazy-upgrade path in login().
     */
    private boolean looksHashed(String storedPassword) {
        return storedPassword != null
                && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
    }

    public User login(String email, String password) {
        // Validation: Check for null/empty and valid format
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return null;
        }

        String e = email.trim();
        String p = password.trim();
        User user = userRepository.findByEmail(e);
        if (user == null || user.getPassword() == null) {
            return null;
        }

        boolean matches;
        if (looksHashed(user.getPassword())) {
            matches = passwordEncoder.matches(p, user.getPassword());
        } else {
            // Legacy plaintext seeded password: compare as before, then silently upgrade it to a
            // BCrypt hash on successful login so it is never stored in plaintext again.
            matches = user.getPassword().trim().equals(p);
            if (matches) {
                user.setPassword(passwordEncoder.encode(p));
                userRepository.update(user);
            }
        }

        if (!matches) {
            return null;
        }
        if (!user.isActive()) {
            return null;
        }
        return user;
    }

    /** Used by the login controller to show a specific "account disabled" message. */
    public boolean isAccountDisabled(String email) {
        if (!isValidEmail(email)) {
            return false;
        }
        User user = userRepository.findByEmail(email.trim());
        return user != null && !user.isActive();
    }

    /** Registers a driver account. Returns an error message, or null when the account was created. */
    public String registerRegularUser(String name, String email, String password, String phone, String licenseNumber) {
        return registerRegularUser(name, email, password, phone, licenseNumber, UserRoles.DRIVER);
    }

    public String registerRegularUser(String name, String email, String password, String phone,
                                      String licenseNumber, String stakeholderRole) {
        String error = validateCommonFields(name, email, password, phone);
        if (error != null) return error;
        if (!isValidLicenseNumber(licenseNumber)) {
            return "License number must be between 5 and 30 characters.";
        }
        if (!UserRoles.isValidUserTierRole(stakeholderRole)) {
            return "Please select a valid role for this account.";
        }

        String userId = UUID.randomUUID().toString();
        User user = userFactory.createUser(
                UserFactory.REGULAR_USER,
                userId,
                name.trim(),
                email.trim(),
                passwordEncoder.encode(password.trim()),
                phone.trim(),
                licenseNumber.trim(),
                stakeholderRole
        );
        userRepository.save(user);
        return null;
    }

    /** Registers an admin-tier account. Returns an error message, or null when it was created. */
    public String registerAdminUser(String name, String email, String password, String phone, String adminCode) {
        return registerAdminUser(name, email, password, phone, adminCode, UserRoles.ADMINISTRATOR);
    }

    public String registerAdminUser(String name, String email, String password, String phone,
                                    String adminCode, String stakeholderRole) {
        String error = validateCommonFields(name, email, password, phone);
        if (error != null) return error;
        if (!isValidAdminCode(adminCode)) {
            return "Admin code is required for administrator-level accounts.";
        }
        if (!UserRoles.isValidAdminTierRole(stakeholderRole)) {
            return "Please select a valid role for this account.";
        }

        String userId = UUID.randomUUID().toString();
        User user = userFactory.createUser(
                UserFactory.ADMIN_USER,
                userId,
                name.trim(),
                email.trim(),
                passwordEncoder.encode(password.trim()),
                phone.trim(),
                adminCode.trim(),
                stakeholderRole
        );
        userRepository.save(user);
        return null;
    }

    /** Updates an account. Returns an error message, or null when the update was saved. */
    public String updateUser(User user) {
        if (user == null) return "User not found.";
        if (!isValidName(user.getName())) return "Full name must be between 2 and 100 characters.";
        if (!isValidEmail(user.getEmail())) return "Please enter a valid email address (e.g. john@example.com).";
        if (!isValidPhone(user.getPhone())) return "Phone number must contain 10 to 15 digits only.";
        if (isEmailTaken(user.getEmail(), user.getUserId())) {
            return "Another account already uses this email address.";
        }
        userRepository.update(user);
        return null;
    }

    /** Deletes an account. Returns an error message, or null when the account was deleted. */
    public String deleteUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return "User not found.";
        if (userRepository.findById(userId) == null) return "User not found.";
        userRepository.delete(userId);
        return null;
    }

    /** Used by the password-reset flow. Always stores the new password hashed. */
    public boolean resetPassword(String userId, String newPassword) {
        if (userId == null || !isValidPassword(newPassword)) {
            return false;
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.update(user);
        return true;
    }
}
