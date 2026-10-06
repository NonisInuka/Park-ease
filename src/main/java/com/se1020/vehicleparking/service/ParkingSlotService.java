package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.repository.ParkingFacilityRepository;
import com.se1020.vehicleparking.repository.ParkingSessionRepository;
import com.se1020.vehicleparking.repository.ParkingSlotRepository;
import com.se1020.vehicleparking.repository.ParkingZoneRepository;
import com.se1020.vehicleparking.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingFacilityRepository facilityRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public ParkingSlotService(ParkingSlotRepository parkingSlotRepository,
                              ParkingFacilityRepository facilityRepository,
                              ParkingZoneRepository zoneRepository,
                              ReservationRepository reservationRepository,
                              ParkingSessionRepository parkingSessionRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.facilityRepository = facilityRepository;
        this.zoneRepository = zoneRepository;
        this.reservationRepository = reservationRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    private boolean isValidSlotNumber(String slotNumber) {
        return slotNumber != null && !slotNumber.trim().isEmpty() && slotNumber.trim().length() <= 20;
    }

    private boolean isValidVehicleType(String type) {
        return VehicleCategories.isValid(type);
    }

    private boolean isValidCategory(String category) {
        return List.of("STANDARD", "ACCESSIBLE", "PREMIUM").contains(normalize(category));
    }

    private boolean isValidRatePerHour(double rate) { return rate > 0 && rate <= 10000; }

    private boolean isValidStatus(String status) {
        return List.of("AVAILABLE", "OCCUPIED", "MAINTENANCE").contains(normalize(status));
    }

    public List<ParkingSlot> getAllSlots() { return parkingSlotRepository.findAll(); }

    public List<ParkingSlot> getAvailableSlots() {
        return parkingSlotRepository.findByStatus("AVAILABLE").stream()
                .filter(this::isInOperationalArea)
                .toList();
    }

    /**
     * A physically OCCUPIED bay can still be reserved for a later non-overlapping period.
     * Facilities/zones that are inactive/under maintenance and slot-level MAINTENANCE are excluded.
     */
    public List<ParkingSlot> getBookableSlots() {
        return parkingSlotRepository.findAll().stream()
                .filter(slot -> !"MAINTENANCE".equals(slot.getStatus()))
                .filter(this::isInOperationalArea)
                .toList();
    }

    public ParkingSlot getSlotById(String slotId) { return parkingSlotRepository.findById(slotId); }

    public String addSlot(String slotNumber, String zoneId, String type,
                          String spaceCategory, double ratePerHour) {
        if (!isValidSlotNumber(slotNumber)) return "Slot number is required and must be 20 characters or fewer.";
        if (!isValidVehicleType(type)) return "Please select a valid vehicle category.";
        if (!isValidCategory(spaceCategory)) return "Please select a valid space category.";
        if (!isValidRatePerHour(ratePerHour)) return "Rate per hour must be between 0.01 and 10000 LKR.";

        ParkingZone zone = zoneRepository.findById(zoneId);
        if (zone == null) return "Please select a valid parking zone.";
        ParkingFacility facility = facilityRepository.findById(zone.getFacilityId());
        if (facility == null) return "The selected zone is not attached to a valid facility.";
        if (parkingSlotRepository.existsByZoneAndSlotNumber(zoneId, slotNumber.trim())) {
            return "This slot number already exists in the selected zone.";
        }

        ParkingSlot slot = new ParkingSlot(UUID.randomUUID().toString(), slotNumber.trim(), zone.getFloor(),
                VehicleCategories.normalize(type), normalize(spaceCategory), facility.getFacilityId(), zone.getZoneId(),
                "AVAILABLE", ratePerHour);
        parkingSlotRepository.save(slot);
        return null;
    }

    public String updateSlot(String slotId, String slotNumber, String zoneId, String type,
                             String spaceCategory, String status, double ratePerHour) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId);
        if (slot == null) return "Parking slot not found.";
        if (!isValidSlotNumber(slotNumber)) return "Slot number is required and must be 20 characters or fewer.";
        if (!isValidVehicleType(type)) return "Please select a valid vehicle category.";
        if (!isValidCategory(spaceCategory)) return "Please select a valid space category.";
        if (!isValidStatus(status)) return "Please select a valid slot status.";
        if (!isValidRatePerHour(ratePerHour)) return "Rate per hour must be between 0.01 and 10000 LKR.";

        ParkingZone zone = zoneRepository.findById(zoneId);
        if (zone == null) return "Please select a valid parking zone.";
        ParkingFacility facility = facilityRepository.findById(zone.getFacilityId());
        if (facility == null) return "The selected zone is not attached to a valid facility.";
        if (parkingSlotRepository.existsByZoneAndSlotNumberExcept(zoneId, slotNumber.trim(), slotId)) {
            return "This slot number already exists in the selected zone.";
        }
        if ("AVAILABLE".equals(normalize(status)) && parkingSessionRepository.hasOpenSessionForSlot(slotId)) {
            return "An occupied slot with an active parking session cannot be marked AVAILABLE.";
        }

        slot.setSlotNumber(slotNumber.trim());
        slot.setFloor(zone.getFloor());
        slot.setType(VehicleCategories.normalize(type));
        slot.setSpaceCategory(normalize(spaceCategory));
        slot.setFacilityId(facility.getFacilityId());
        slot.setZoneId(zone.getZoneId());
        slot.setStatus(normalize(status));
        slot.setRatePerHour(ratePerHour);
        parkingSlotRepository.update(slot);
        return null;
    }

    /** Used by parking-session operations; deliberately keeps this small and compatible. */
    public void updateSlotStatus(String slotId, String status) {
        if (slotId == null || slotId.isBlank() || !isValidStatus(status)) return;
        ParkingSlot slot = parkingSlotRepository.findById(slotId);
        if (slot == null) return;
        slot.setStatus(normalize(status));
        parkingSlotRepository.update(slot);
    }

    public String deleteSlot(String slotId) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId);
        if (slot == null) return "Parking slot not found.";
        if (parkingSessionRepository.hasOpenSessionForSlot(slotId) || "OCCUPIED".equals(slot.getStatus())) {
            return "An occupied parking slot cannot be removed.";
        }
        if (!reservationRepository.findBySlotId(slotId).isEmpty()) {
            return "This parking slot has reservation history and cannot be deleted. Set it to MAINTENANCE instead.";
        }
        parkingSlotRepository.delete(slotId);
        return null;
    }

    /** Returns true only when the slot belongs to an ACTIVE facility and ACTIVE zone. */
    public boolean isOperationalForUse(String slotId) {
        ParkingSlot slot = parkingSlotRepository.findById(slotId);
        return slot != null && isInOperationalArea(slot);
    }

    public String getFacilityName(ParkingSlot slot) {
        ParkingFacility facility = slot == null ? null : facilityRepository.findById(slot.getFacilityId());
        return facility != null ? facility.getName() : "Unassigned Facility";
    }

    public String getZoneName(ParkingSlot slot) {
        ParkingZone zone = slot == null ? null : zoneRepository.findById(slot.getZoneId());
        return zone != null ? zone.getName() : "Unassigned Zone";
    }

    private boolean isInOperationalArea(ParkingSlot slot) {
        if (slot.getFacilityId() == null || slot.getZoneId() == null) return true; // legacy-safe until backfill runs
        ParkingFacility facility = facilityRepository.findById(slot.getFacilityId());
        ParkingZone zone = zoneRepository.findById(slot.getZoneId());
        return facility != null && zone != null
                && "ACTIVE".equals(facility.getStatus())
                && "ACTIVE".equals(zone.getStatus());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
