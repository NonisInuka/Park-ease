package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.UserRoles;
import com.se1020.vehicleparking.service.ParkingSessionService;
import com.se1020.vehicleparking.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parking Operations & Session Management.
 *
 * Management routes live under /operations/** so Parking Staff (USER tier) and Operations Manager
 * / Administrator (ADMIN tier) can share the same workflow without weakening /admin/** security.
 */
@Controller
public class ParkingSessionController {

    @Autowired
    private ParkingSessionService parkingSessionService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/operations/sessions")
    public String allSessions(@RequestParam(required = false) String query,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("loggedUser");
        parkingSessionService.refreshOverstayedSessions();
        model.addAttribute("readyForCheckIn", parkingSessionService.getReservationsReadyForCheckIn());
        model.addAttribute("sessions", parkingSessionService.getAllSessions());
        model.addAttribute("checkInEarlyMinutes", ParkingSessionService.EARLY_CHECK_IN_MINUTES);
        model.addAttribute("canCorrectSessions",
                UserRoles.canCorrectParkingSessions(user.getRole(), user.getStakeholderRole()));

        String verificationQuery = query == null ? "" : query.trim();
        model.addAttribute("verificationQuery", verificationQuery);
        model.addAttribute("verificationPerformed", !verificationQuery.isEmpty());

        if (!verificationQuery.isEmpty()) {
            List<Reservation> verificationResults =
                    reservationService.findForOperationsVerification(verificationQuery);
            Map<String, String> verificationErrors = new LinkedHashMap<>();
            for (Reservation reservation : verificationResults) {
                verificationErrors.put(reservation.getReservationId(),
                        parkingSessionService.getCheckInEligibility(reservation.getReservationId()));
            }
            model.addAttribute("verificationResults", verificationResults);
            model.addAttribute("verificationErrors", verificationErrors);
        }

        return "session/admin-session-list";
    }

    /** Backward-compatible admin link from the first session implementation. */
    @GetMapping("/admin/sessions")
    public String legacyAdminSessions() {
        return "redirect:/operations/sessions";
    }

    @PostMapping("/operations/sessions/checkin/{reservationId}")
    public String checkIn(@PathVariable String reservationId, RedirectAttributes redirectAttributes) {
        String error = parkingSessionService.checkIn(reservationId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Vehicle checked in. Parking session started.");
        }
        return "redirect:/operations/sessions";
    }

    @PostMapping("/operations/sessions/checkout/{sessionId}")
    public String checkOut(@PathVariable String sessionId, RedirectAttributes redirectAttributes) {
        String error = parkingSessionService.checkOut(sessionId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Vehicle checked out. Session and reservation completed.");
        }
        return "redirect:/operations/sessions";
    }

    @PostMapping("/operations/sessions/delete/{sessionId}")
    public String removeIncorrectSession(@PathVariable String sessionId,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        if (!UserRoles.canCorrectParkingSessions(user.getRole(), user.getStakeholderRole())) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to remove parking sessions.");
            return "redirect:/operations/sessions";
        }

        String error = parkingSessionService.removeIncorrectSession(sessionId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Incorrect parking session removed.");
        }
        return "redirect:/operations/sessions";
    }

    // Driver's own session history (read-only).
    @GetMapping("/user/sessions")
    public String mySessions(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("sessions", parkingSessionService.getSessionsByUser(user.getUserId()));
        return "session/my-sessions";
    }
}
