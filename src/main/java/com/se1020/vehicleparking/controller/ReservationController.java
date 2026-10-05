package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.service.ParkingSlotService;
import com.se1020.vehicleparking.service.ReservationService;
import com.se1020.vehicleparking.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private VehicleService vehicleService;

    // READ - User views own reservations
    @GetMapping("/user/reservations")
    public String myReservations(@RequestParam(required = false, defaultValue = "") String q,
                                 @RequestParam(required = false, defaultValue = "") String status,
                                 HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        List<Reservation> reservations = filterReservations(
                reservationService.getReservationsByUser(user.getUserId()), q, status);
        Set<String> checkedInReservationIds = reservations.stream()
                .filter(reservation -> reservationService.hasOpenParkingSession(reservation.getReservationId()))
                .map(Reservation::getReservationId)
                .collect(Collectors.toSet());
        model.addAttribute("reservations", reservations);
        model.addAttribute("checkedInReservationIds", checkedInReservationIds);
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", status == null ? "" : status.trim().toUpperCase());
        return "reservation/my-reservations";
    }

    // READ - Admin views all reservations
    @GetMapping("/admin/reservations")
    public String allReservations(@RequestParam(required = false, defaultValue = "") String q,
                                  @RequestParam(required = false, defaultValue = "") String status,
                                  Model model) {
        model.addAttribute("reservations", filterReservations(reservationService.getAllReservations(), q, status));
        model.addAttribute("q", q);
        model.addAttribute("statusFilter", status == null ? "" : status.trim().toUpperCase());
        return "reservation/admin-reservation-list";
    }

    private List<Reservation> filterReservations(List<Reservation> source, String q, String status) {
        String query = q == null ? "" : q.trim().toLowerCase();
        String statusFilter = status == null ? "" : status.trim().toUpperCase();
        return source.stream()
                .filter(reservation -> query.isEmpty()
                        || containsIgnoreCase(reservation.getReservationId(), query)
                        || containsIgnoreCase(reservation.getVehiclePlate(), query)
                        || containsIgnoreCase(reservation.getSlotNumber(), query)
                        || containsIgnoreCase(reservation.getReservationDate(), query)
                        || containsIgnoreCase(reservation.getUserId(), query))
                .filter(reservation -> statusFilter.isEmpty()
                        || statusFilter.equalsIgnoreCase(reservation.getStatus()))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String lowerCaseQuery) {
        return value != null && value.toLowerCase().contains(lowerCaseQuery);
    }

    // READ - Search/browse available parking slots to book
    @GetMapping("/user/reservations/book")
    public String bookPage(@RequestParam(required = false) String slotId,
                           HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("slots", parkingSlotService.getBookableSlots());
        model.addAttribute("vehicles", vehicleService.getVehiclesByOwner(user.getUserId()));
        model.addAttribute("vehicleCategoryLabels", VehicleCategories.options());
        model.addAttribute("preselectedSlotId", slotId);
        return "reservation/book-slot";
    }

    // CREATE - Book slot submit
    @PostMapping("/user/reservations/book")
    public String bookSlot(@RequestParam String vehiclePlate,
                           @RequestParam String slotId,
                           @RequestParam String reservationDate,
                           @RequestParam String startTime,
                           @RequestParam int durationHours,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = reservationService.createReservation(user.getUserId(), vehiclePlate,
                slotId, reservationDate, startTime, durationHours);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/reservations/book?slotId=" + slotId;
        }
        redirectAttributes.addFlashAttribute("success", "Reservation confirmed.");
        return "redirect:/user/reservations";
    }

    // UPDATE - Edit reservation page
    @GetMapping("/user/reservations/edit/{reservationId}")
    public String editReservationPage(@PathVariable String reservationId, HttpSession session,
                                       Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Reservation reservation = reservationService.getOwnReservation(reservationId, user.getUserId());
        if (reservation == null) {
            redirectAttributes.addFlashAttribute("error", "Reservation not found.");
            return "redirect:/user/reservations";
        }
        model.addAttribute("reservation", reservation);
        return "reservation/edit-reservation";
    }

    // UPDATE - Edit reservation submit
    @PostMapping("/user/reservations/update")
    public String updateReservation(@RequestParam String reservationId,
                                    @RequestParam String startTime,
                                    @RequestParam int durationHours,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = reservationService.updateReservation(reservationId, user.getUserId(), startTime, durationHours);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/reservations/edit/" + reservationId;
        }
        redirectAttributes.addFlashAttribute("success", "Reservation updated.");
        return "redirect:/user/reservations";
    }

    // DELETE - Cancel reservation
    @PostMapping("/user/reservations/cancel/{reservationId}")
    public String cancelReservation(@PathVariable String reservationId, HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = reservationService.cancelReservation(reservationId, user.getUserId());
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled.");
        }
        return "redirect:/user/reservations";
    }

    // DELETE - Admin hard deletes reservation
    @PostMapping("/admin/reservations/delete/{reservationId}")
    public String deleteReservation(@PathVariable String reservationId,
                                    RedirectAttributes redirectAttributes) {
        String error = reservationService.deleteReservation(reservationId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Reservation deleted.");
        }
        return "redirect:/admin/reservations";
    }
}
