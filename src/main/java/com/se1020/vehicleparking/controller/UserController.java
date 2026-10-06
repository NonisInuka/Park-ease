package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.UserRoles;
import com.se1020.vehicleparking.service.PasswordResetService;
import com.se1020.vehicleparking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/*
CREATE - POST
read - GET
UPDATE - PUT
DELETE - DELETE
*/

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    // READ - Login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false, defaultValue = "false") boolean expired, Model model) {
        if (expired) {
            model.addAttribute("info", "Your session expired after 30 minutes of inactivity. Please sign in again.");
        }
        return "auth/login";
    }

    // READ - Login submit
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpSession session, Model model) {
        User user = userService.login(email, password);
        if (user != null) {
            // Issue a fresh session id on login so a pre-login session id can't be reused (session fixation).
            request.changeSessionId();
            session.setAttribute("loggedUser", user);
            return "redirect:" + user.getDashboardPath();
        }
        if (userService.isAccountDisabled(email)) {
            model.addAttribute("error", "This account has been disabled. Please contact an administrator.");
        } else {
            model.addAttribute("error", "Invalid credentials");
        }
        return "auth/login";
    }

    // CREATE - Register page
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // CREATE - Register submit (public self-registration is always a Driver account)
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String phone,
                           @RequestParam String licenseNumber,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String error = userService.registerRegularUser(name, email, password, phone, licenseNumber);
        if (error != null) {
            // Show the error on the same page and keep what the user typed (never the password).
            model.addAttribute("error", error);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            model.addAttribute("licenseNumber", licenseNumber);
            return "auth/register";
        }
        redirectAttributes.addFlashAttribute("info", "Account created successfully. Please sign in.");
        return "redirect:/login";
    }

    // READ - Admin views all users
    @GetMapping("/admin/users")
    public String getAllUsers(@RequestParam(required = false, defaultValue = "") String q,
                              @RequestParam(required = false, defaultValue = "") String status,
                              @RequestParam(required = false, defaultValue = "") String stakeholderRole,
                              Model model) {
        String query = q.trim().toLowerCase();
        String statusFilter = status.trim().toUpperCase();
        String stakeholderFilter = stakeholderRole.trim().toUpperCase();

        List<User> users = userService.getAllUsers().stream()
                .filter(user -> query.isEmpty()
                        || containsIgnoreCase(user.getName(), query)
                        || containsIgnoreCase(user.getEmail(), query)
                        || containsIgnoreCase(user.getPhone(), query))
                .filter(user -> statusFilter.isEmpty()
                        || statusFilter.equalsIgnoreCase(user.getStatus() == null ? "ACTIVE" : user.getStatus()))
                .filter(user -> stakeholderFilter.isEmpty()
                        || stakeholderFilter.equalsIgnoreCase(user.getStakeholderRole()))
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("stakeholderRoleFilter", stakeholderFilter);
        model.addAttribute("allStakeholderRoles", java.util.stream.Stream.concat(
                UserRoles.ADMIN_TIER_ROLES.stream(), UserRoles.USER_TIER_ROLES.stream()).toList());
        return "user/admin-user-list";
    }

    private boolean containsIgnoreCase(String value, String lowerCaseQuery) {
        return value != null && value.toLowerCase().contains(lowerCaseQuery);
    }

    // READ - View own profile
    @GetMapping("/user/profile")
    public String viewProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("user", user);
        return "user/profile";
    }

    // UPDATE - Edit profile page
    @GetMapping("/user/profile/edit")
    public String editProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    // UPDATE - Edit profile submit
    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("loggedUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // Always act on the currently authenticated user's own record - never on a userId
        // supplied by the client - so one user cannot edit another user's profile by tampering
        // with the form.
        User user = userService.getUserById(sessionUser.getUserId());
        if (user == null) {
            return "redirect:/login";
        }
        String previousName = user.getName();
        String previousPhone = user.getPhone();
        user.setName(name == null ? null : name.trim());
        user.setPhone(phone == null ? null : phone.trim());
        String error = userService.updateUser(user);
        if (error != null) {
            // Restore the in-memory values so a rejected edit is not shown as saved.
            user.setName(previousName);
            user.setPhone(previousPhone);
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/profile/edit";
        }
        session.setAttribute("loggedUser", user);
        redirectAttributes.addFlashAttribute("success", "Profile updated.");
        return "redirect:/user/profile";
    }

    // CREATE - Admin add user page
    @GetMapping("/admin/users/add")
    public String addUserPage(Model model) {
        model.addAttribute("adminTierRoles", UserRoles.ADMIN_TIER_ROLES);
        model.addAttribute("userTierRoles", UserRoles.USER_TIER_ROLES);
        return "user/add-user";
    }

    // CREATE - Admin add user submit
    @PostMapping("/admin/users/add")
    public String addUser(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String phone,
                          @RequestParam String role,
                          @RequestParam(required = false, defaultValue = "") String licenseNumber,
                          @RequestParam(required = false, defaultValue = "") String adminCode,
                          RedirectAttributes redirectAttributes) {
        String error;
        if (UserRoles.isValidAdminTierRole(role)) {
            error = userService.registerAdminUser(name, email, password, phone, adminCode, role);
        } else {
            error = userService.registerRegularUser(name, email, password, phone, licenseNumber, role);
        }
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/users/add";
        }
        redirectAttributes.addFlashAttribute("success", "User account created.");
        return "redirect:/admin/users";
    }

    // UPDATE - Admin edit user page
    @GetMapping("/admin/users/edit/{userId}")
    public String adminEditUserPage(@PathVariable String userId, Model model) {
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("availableRoles", user != null ? UserRoles.availableRolesFor(user.getRole()) : UserRoles.USER_TIER_ROLES);
        return "user/edit-user";
    }

    // UPDATE - Admin edit user submit
    @PostMapping("/admin/users/update")
    public String adminUpdateUser(@RequestParam String userId,
                                   @RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String phone,
                                   @RequestParam(required = false) String stakeholderRole,
                                   @RequestParam(required = false, defaultValue = "ACTIVE") String status,
                                   RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
        user.setName(name == null ? null : name.trim());
        user.setEmail(email == null ? null : email.trim());
        user.setPhone(phone == null ? null : phone.trim());
        user.setStatus("DISABLED".equalsIgnoreCase(status) ? "DISABLED" : "ACTIVE");

        // A role can only be reassigned within the roles valid for this account's existing kind
        // (ADMIN-kind vs USER-kind) - see UserRoles.java. Anything else is ignored rather than
        // silently accepted, since converting an account between kinds isn't supported yet.
        boolean roleAllowed = "ADMIN".equals(user.getRole())
                ? UserRoles.isValidAdminTierRole(stakeholderRole)
                : UserRoles.isValidUserTierRole(stakeholderRole);
        if (roleAllowed) {
            user.setStakeholderRole(stakeholderRole);
        }

        String error = userService.updateUser(user);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/users/edit/" + userId;
        }
        redirectAttributes.addFlashAttribute("success", "User updated.");
        return "redirect:/admin/users";
    }

    // DELETE - Admin deletes user
    @PostMapping("/admin/users/delete/{userId}")
    public String deleteUser(@PathVariable String userId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User current = (User) session.getAttribute("loggedUser");
        if (current != null && current.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account while signed in.");
            return "redirect:/admin/users";
        }
        String error = userService.deleteUser(userId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "User deleted.");
        }
        return "redirect:/admin/users";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- Password recovery (local-development-safe: no email/SMS server exists yet) ---

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        User user = userService.getUserByEmail(email);
        model.addAttribute("submitted", true);
        // No SMTP/SMS integration in this phase, so the link is shown on-screen instead of emailed.
        // To avoid revealing which emails are registered, the page looks identical for unknown
        // emails: they get a link too, but its token was never stored, so it is reported as invalid.
        String token;
        if (user != null) {
            token = passwordResetService.createToken(user.getUserId());
            System.out.println("Password reset requested for " + user.getEmail() + " -> /reset-password?token=" + token);
        } else {
            token = java.util.UUID.randomUUID().toString();
        }
        model.addAttribute("resetLink", "/reset-password?token=" + token);
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        String userId = passwordResetService.resolveUserId(token);
        if (userId == null) {
            model.addAttribute("invalid", true);
        } else {
            model.addAttribute("token", token);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String userId = passwordResetService.resolveUserId(token);
        if (userId == null) {
            model.addAttribute("invalid", true);
            return "auth/reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "The two passwords do not match.");
            return "auth/reset-password";
        }
        boolean success = userService.resetPassword(userId, password);
        if (!success) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "auth/reset-password";
        }
        passwordResetService.consumeToken(token);
        redirectAttributes.addFlashAttribute("info", "Password updated. Please sign in with your new password.");
        return "redirect:/login";
    }
}
