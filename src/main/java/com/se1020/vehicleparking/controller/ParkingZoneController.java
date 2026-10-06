package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.service.ParkingFacilityService;
import com.se1020.vehicleparking.service.ParkingZoneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/zones")
public class ParkingZoneController {

    private final ParkingZoneService zoneService;
    private final ParkingFacilityService facilityService;

    public ParkingZoneController(ParkingZoneService zoneService, ParkingFacilityService facilityService) {
        this.zoneService = zoneService;
        this.facilityService = facilityService;
    }

    @GetMapping
    public String list(Model model) {
        var facilities = facilityService.getAllFacilities();
        Map<String, String> facilityNames = facilities.stream()
                .collect(Collectors.toMap(f -> f.getFacilityId(), f -> f.getName()));
        model.addAttribute("zones", zoneService.getAllZones());
        model.addAttribute("facilityNames", facilityNames);
        return "zone/admin-zone-list";
    }

    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("facilities", facilityService.getAllFacilities());
        return "zone/add-zone";
    }

    @PostMapping("/add")
    public String add(@RequestParam String facilityId,
                      @RequestParam String name,
                      @RequestParam String floor,
                      @RequestParam(required = false) String description,
                      RedirectAttributes redirectAttributes) {
        String error = zoneService.addZone(facilityId, name, floor, description);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/zones/add";
        }
        redirectAttributes.addFlashAttribute("success", "Parking zone created.");
        return "redirect:/admin/zones";
    }

    @GetMapping("/edit/{zoneId}")
    public String editPage(@PathVariable String zoneId, Model model, RedirectAttributes redirectAttributes) {
        ParkingZone zone = zoneService.getZoneById(zoneId);
        if (zone == null) {
            redirectAttributes.addFlashAttribute("error", "Parking zone not found.");
            return "redirect:/admin/zones";
        }
        model.addAttribute("zone", zone);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        return "zone/edit-zone";
    }

    @PostMapping("/update")
    public String update(@RequestParam String zoneId,
                         @RequestParam String facilityId,
                         @RequestParam String name,
                         @RequestParam String floor,
                         @RequestParam(required = false) String description,
                         @RequestParam String status,
                         RedirectAttributes redirectAttributes) {
        String error = zoneService.updateZone(zoneId, facilityId, name, floor, description, status);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/zones/edit/" + zoneId;
        }
        redirectAttributes.addFlashAttribute("success", "Parking zone updated.");
        return "redirect:/admin/zones";
    }

    @PostMapping("/delete/{zoneId}")
    public String delete(@PathVariable String zoneId, RedirectAttributes redirectAttributes) {
        String error = zoneService.deleteZone(zoneId);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Parking zone removed.");
        return "redirect:/admin/zones";
    }
}
