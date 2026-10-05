package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.service.ParkingFacilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/facilities")
public class ParkingFacilityController {

    private final ParkingFacilityService facilityService;

    public ParkingFacilityController(ParkingFacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("facilities", facilityService.getAllFacilities());
        return "facility/admin-facility-list";
    }

    @GetMapping("/add")
    public String addPage() {
        return "facility/add-facility";
    }

    @PostMapping("/add")
    public String add(@RequestParam String name,
                      @RequestParam String address,
                      @RequestParam(required = false) String description,
                      RedirectAttributes redirectAttributes) {
        String error = facilityService.addFacility(name, address, description);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/facilities/add";
        }
        redirectAttributes.addFlashAttribute("success", "Parking facility created.");
        return "redirect:/admin/facilities";
    }

    @GetMapping("/edit/{facilityId}")
    public String editPage(@PathVariable String facilityId, Model model, RedirectAttributes redirectAttributes) {
        ParkingFacility facility = facilityService.getFacilityById(facilityId);
        if (facility == null) {
            redirectAttributes.addFlashAttribute("error", "Parking facility not found.");
            return "redirect:/admin/facilities";
        }
        model.addAttribute("facility", facility);
        return "facility/edit-facility";
    }

    @PostMapping("/update")
    public String update(@RequestParam String facilityId,
                         @RequestParam String name,
                         @RequestParam String address,
                         @RequestParam(required = false) String description,
                         @RequestParam String status,
                         RedirectAttributes redirectAttributes) {
        String error = facilityService.updateFacility(facilityId, name, address, description, status);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/facilities/edit/" + facilityId;
        }
        redirectAttributes.addFlashAttribute("success", "Parking facility updated.");
        return "redirect:/admin/facilities";
    }

    @PostMapping("/delete/{facilityId}")
    public String delete(@PathVariable String facilityId, RedirectAttributes redirectAttributes) {
        String error = facilityService.deleteFacility(facilityId);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Parking facility removed.");
        return "redirect:/admin/facilities";
    }
}
