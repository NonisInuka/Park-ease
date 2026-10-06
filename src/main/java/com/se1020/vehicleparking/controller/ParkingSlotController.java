package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.service.ParkingFacilityService;
import com.se1020.vehicleparking.service.ParkingSlotService;
import com.se1020.vehicleparking.service.ParkingZoneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;
    private final ParkingFacilityService facilityService;
    private final ParkingZoneService zoneService;

    public ParkingSlotController(ParkingSlotService parkingSlotService,
                                 ParkingFacilityService facilityService,
                                 ParkingZoneService zoneService) {
        this.parkingSlotService = parkingSlotService;
        this.facilityService = facilityService;
        this.zoneService = zoneService;
    }

    @GetMapping("/admin/slots")
    public String getAllSlots(@RequestParam(required = false, defaultValue = "") String q,
                              @RequestParam(required = false, defaultValue = "") String facilityId,
                              @RequestParam(required = false, defaultValue = "") String zoneId,
                              @RequestParam(required = false, defaultValue = "") String type,
                              @RequestParam(required = false, defaultValue = "") String spaceCategory,
                              @RequestParam(required = false, defaultValue = "") String status,
                              @RequestParam(required = false) Double maxRate,
                              Model model) {
        List<ParkingSlot> slots = filterSlots(parkingSlotService.getAllSlots(), q, facilityId, zoneId, type, spaceCategory, status, maxRate);
        putSlotViewData(model, slots);
        putFilterState(model, q, facilityId, zoneId, type, spaceCategory, status, maxRate);
        return "slot/admin-slot-list";
    }

    @GetMapping("/user/slots")
    public String getAvailableSlots(@RequestParam(required = false, defaultValue = "") String q,
                                    @RequestParam(required = false, defaultValue = "") String facilityId,
                                    @RequestParam(required = false, defaultValue = "") String zoneId,
                                    @RequestParam(required = false, defaultValue = "") String type,
                                    @RequestParam(required = false, defaultValue = "") String spaceCategory,
                                    @RequestParam(required = false) Double maxRate,
                                    Model model) {
        List<ParkingSlot> slots = filterSlots(parkingSlotService.getAvailableSlots(), q, facilityId, zoneId, type, spaceCategory, "", maxRate);
        putSlotViewData(model, slots);
        putFilterState(model, q, facilityId, zoneId, type, spaceCategory, "", maxRate);
        return "slot/available-slots";
    }

    private List<ParkingSlot> filterSlots(List<ParkingSlot> source, String q, String facilityId, String zoneId,
                                          String type, String spaceCategory, String status, Double maxRate) {
        String query = q == null ? "" : q.trim().toLowerCase();
        String facilityFilter = facilityId == null ? "" : facilityId.trim();
        String zoneFilter = zoneId == null ? "" : zoneId.trim();
        String typeFilter = type == null ? "" : type.trim().toUpperCase();
        String categoryFilter = spaceCategory == null ? "" : spaceCategory.trim().toUpperCase();
        String statusFilter = status == null ? "" : status.trim().toUpperCase();

        Map<String, String> facilityNames = facilityService.getAllFacilities().stream()
                .collect(Collectors.toMap(ParkingFacility::getFacilityId, ParkingFacility::getName));
        Map<String, String> zoneNames = zoneService.getAllZones().stream()
                .collect(Collectors.toMap(ParkingZone::getZoneId, ParkingZone::getName));

        return source.stream()
                .filter(slot -> query.isEmpty()
                        || containsIgnoreCase(slot.getSlotNumber(), query)
                        || containsIgnoreCase(slot.getFloor(), query)
                        || containsIgnoreCase(facilityNames.get(slot.getFacilityId()), query)
                        || containsIgnoreCase(zoneNames.get(slot.getZoneId()), query))
                .filter(slot -> facilityFilter.isEmpty() || facilityFilter.equals(slot.getFacilityId()))
                .filter(slot -> zoneFilter.isEmpty() || zoneFilter.equals(slot.getZoneId()))
                .filter(slot -> typeFilter.isEmpty() || typeFilter.equalsIgnoreCase(slot.getType()))
                .filter(slot -> categoryFilter.isEmpty() || categoryFilter.equalsIgnoreCase(slot.getSpaceCategory()))
                .filter(slot -> statusFilter.isEmpty() || statusFilter.equalsIgnoreCase(slot.getStatus()))
                .filter(slot -> maxRate == null || maxRate < 0 || slot.getRatePerHour() <= maxRate)
                .toList();
    }

    private boolean containsIgnoreCase(String value, String lowerCaseQuery) {
        return value != null && value.toLowerCase().contains(lowerCaseQuery);
    }

    private void putFilterState(Model model, String q, String facilityId, String zoneId, String type,
                                String spaceCategory, String status, Double maxRate) {
        model.addAttribute("q", q);
        model.addAttribute("facilityFilter", facilityId);
        model.addAttribute("zoneFilter", zoneId);
        model.addAttribute("typeFilter", type);
        model.addAttribute("spaceCategoryFilter", spaceCategory);
        model.addAttribute("statusFilter", status);
        model.addAttribute("maxRateFilter", maxRate);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        model.addAttribute("zones", zoneService.getAllZones());
        model.addAttribute("vehicleCategories", VehicleCategories.options());
    }

    private void putSlotViewData(Model model, List<ParkingSlot> slots) {
        model.addAttribute("slots", slots);

        Map<String, String> facilityNames = facilityService.getAllFacilities().stream()
                .collect(Collectors.toMap(ParkingFacility::getFacilityId, ParkingFacility::getName));
        Map<String, String> zoneNames = zoneService.getAllZones().stream()
                .collect(Collectors.toMap(ParkingZone::getZoneId, ParkingZone::getName));

        // Group by facility + zone instead of only floor so two facilities with a "Ground" floor
        // never get visually mixed into the same section.
        Map<String, List<ParkingSlot>> byArea = new LinkedHashMap<>();
        for (ParkingSlot slot : slots) {
            String facilityName = facilityNames.getOrDefault(slot.getFacilityId(), "Unassigned Facility");
            String zoneName = zoneNames.getOrDefault(slot.getZoneId(), "Unassigned Zone");
            String floor = slot.getFloor() == null ? "Unspecified" : slot.getFloor();
            String key = facilityName + " — " + zoneName + " (" + floor + ")";
            byArea.computeIfAbsent(key, ignored -> new java.util.ArrayList<>()).add(slot);
        }
        byArea.values().forEach(list -> list.sort(Comparator.comparing(ParkingSlot::getSlotNumber)));

        model.addAttribute("slotsByArea", byArea);
        model.addAttribute("facilityNames", facilityNames);
        model.addAttribute("zoneNames", zoneNames);
        model.addAttribute("vehicleCategoryLabels", VehicleCategories.options());
    }

    @GetMapping("/admin/slots/add")
    public String addSlotPage(Model model) {
        addFormReferenceData(model);
        return "slot/add-slot";
    }

    @PostMapping("/admin/slots/add")
    public String addSlot(@RequestParam String slotNumber,
                          @RequestParam String zoneId,
                          @RequestParam String type,
                          @RequestParam String spaceCategory,
                          @RequestParam double ratePerHour,
                          RedirectAttributes redirectAttributes) {
        String error = parkingSlotService.addSlot(slotNumber, zoneId, type, spaceCategory, ratePerHour);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/slots/add";
        }
        redirectAttributes.addFlashAttribute("success", "Parking slot created.");
        return "redirect:/admin/slots";
    }

    @GetMapping("/admin/slots/edit/{slotId}")
    public String editSlotPage(@PathVariable String slotId, Model model, RedirectAttributes redirectAttributes) {
        ParkingSlot slot = parkingSlotService.getSlotById(slotId);
        if (slot == null) {
            redirectAttributes.addFlashAttribute("error", "Parking slot not found.");
            return "redirect:/admin/slots";
        }
        model.addAttribute("slot", slot);
        addFormReferenceData(model);
        return "slot/edit-slot";
    }

    @PostMapping("/admin/slots/update")
    public String updateSlot(@RequestParam String slotId,
                             @RequestParam String slotNumber,
                             @RequestParam String zoneId,
                             @RequestParam String type,
                             @RequestParam String spaceCategory,
                             @RequestParam String status,
                             @RequestParam double ratePerHour,
                             RedirectAttributes redirectAttributes) {
        String error = parkingSlotService.updateSlot(slotId, slotNumber, zoneId, type, spaceCategory, status, ratePerHour);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/slots/edit/" + slotId;
        }
        redirectAttributes.addFlashAttribute("success", "Parking slot updated.");
        return "redirect:/admin/slots";
    }

    @PostMapping("/admin/slots/delete/{slotId}")
    public String deleteSlot(@PathVariable String slotId, RedirectAttributes redirectAttributes) {
        String error = parkingSlotService.deleteSlot(slotId);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Parking slot removed.");
        return "redirect:/admin/slots";
    }

    private void addFormReferenceData(Model model) {
        model.addAttribute("facilities", facilityService.getAllFacilities());
        model.addAttribute("zones", zoneService.getAllZones());
        Map<String, String> facilityNames = facilityService.getAllFacilities().stream()
                .collect(Collectors.toMap(ParkingFacility::getFacilityId, ParkingFacility::getName));
        model.addAttribute("facilityNames", facilityNames);
        model.addAttribute("vehicleCategories", VehicleCategories.options());
    }
}
