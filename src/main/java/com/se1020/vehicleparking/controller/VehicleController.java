package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.Vehicle;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    /** True when the vehicle exists and belongs to the signed-in user. */
    private boolean ownsVehicle(HttpSession session, String vehicleId) {
        User user = (User) session.getAttribute("loggedUser");
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        return user != null && vehicle != null && user.getUserId().equals(vehicle.getOwnerId());
    }

    // READ - User views own vehicles.json
    @GetMapping("/user/vehicles")
    public String myVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute(
            "vehicles",
            vehicleService.getVehiclesByOwner(user.getUserId())
        );
        model.addAttribute("vehicleCategoryLabels", VehicleCategories.options());
        return "vehicle/my-vehicles";
    }

    // READ - Admin views all vehicles.json
    @GetMapping("/admin/vehicles")
    public String allVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("vehicleCategoryLabels", VehicleCategories.options());
        return "vehicle/admin-vehicle-list";
    }

    // CREATE - Add vehicle page
    @GetMapping("/user/vehicles/add")
    public String addVehiclePage(Model model) {
        model.addAttribute("vehicleCategories", VehicleCategories.options());
        return "vehicle/add-vehicle";
    }

    // CREATE - Add vehicle submit
    @PostMapping("/user/vehicles/add")
    public String addVehicle(
        @RequestParam String plateNumber,
        @RequestParam String make,
        @RequestParam String model,
        @RequestParam String color,
        @RequestParam(required = false, defaultValue = "") String vehicleUrl,
        @RequestParam String type,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("loggedUser");
        String error = vehicleService.addVehicle(
            user.getUserId(),
            plateNumber,
            make,
            model,
            color,
            vehicleUrl,
            type
        );
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/vehicles/add";
        }
        redirectAttributes.addFlashAttribute("success", "Vehicle added.");
        return "redirect:/user/vehicles";
    }

    // UPDATE - Edit vehicle page
    @GetMapping("/user/vehicles/edit/{vehicleId}")
    public String editVehiclePage(@PathVariable String vehicleId, Model model,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        if (!ownsVehicle(session, vehicleId)) {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found.");
            return "redirect:/user/vehicles";
        }
        model.addAttribute("vehicle", vehicleService.getVehicleById(vehicleId));
        model.addAttribute("vehicleCategories", VehicleCategories.options());
        return "vehicle/edit-vehicle";
    }

    // UPDATE - Edit vehicle submit
    @PostMapping("/user/vehicles/update")
    public String updateVehicle(
        @RequestParam String vehicleId,
        @RequestParam String plateNumber,
        @RequestParam String make,
        @RequestParam String model,
        @RequestParam String color,
        @RequestParam(required = false, defaultValue = "") String vehicleUrl,
        @RequestParam String type,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (!ownsVehicle(session, vehicleId)) {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found.");
            return "redirect:/user/vehicles";
        }
        String error = vehicleService.updateVehicle(
            vehicleId,
            plateNumber,
            make,
            model,
            color,
            vehicleUrl,
            type
        );
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/vehicles/edit/" + vehicleId;
        }
        redirectAttributes.addFlashAttribute("success", "Vehicle updated.");
        return "redirect:/user/vehicles";
    }

    // DELETE - Driver deletes one of their own vehicles
    @PostMapping("/user/vehicles/delete/{vehicleId}")
    public String deleteVehicle(
        @PathVariable String vehicleId,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (!ownsVehicle(session, vehicleId)) {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found.");
            return "redirect:/user/vehicles";
        }
        String error = vehicleService.deleteVehicle(vehicleId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Vehicle deleted.");
        }
        return "redirect:/user/vehicles";
    }

    // DELETE - Admin deletes any vehicle (the /admin/** path is reachable only by admin accounts)
    @PostMapping("/admin/vehicles/delete/{vehicleId}")
    public String adminDeleteVehicle(
        @PathVariable String vehicleId,
        RedirectAttributes redirectAttributes
    ) {
        String error = vehicleService.deleteVehicle(vehicleId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Vehicle deleted.");
        }
        return "redirect:/admin/vehicles";
    }

    // UPDATE - Admin edit vehicle page
    @GetMapping("/admin/vehicles/edit/{vehicleId}")
    public String adminEditVehiclePage(
        @PathVariable String vehicleId,
        Model model
    ) {
        model.addAttribute("vehicle", vehicleService.getVehicleById(vehicleId));
        model.addAttribute("vehicleCategories", VehicleCategories.options());
        model.addAttribute("adminEdit", true);
        return "vehicle/edit-vehicle";
    }

    // UPDATE - Admin edit vehicle submit
    @PostMapping("/admin/vehicles/update")
    public String adminUpdateVehicle(
        @RequestParam String vehicleId,
        @RequestParam String plateNumber,
        @RequestParam String make,
        @RequestParam String model,
        @RequestParam String color,
        @RequestParam(required = false, defaultValue = "") String vehicleUrl,
        @RequestParam String type,
        RedirectAttributes redirectAttributes
    ) {
        String error = vehicleService.updateVehicle(
            vehicleId,
            plateNumber,
            make,
            model,
            color,
            vehicleUrl,
            type
        );
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/vehicles/edit/" + vehicleId;
        }
        redirectAttributes.addFlashAttribute("success", "Vehicle updated.");
        return "redirect:/admin/vehicles";
    }
}
