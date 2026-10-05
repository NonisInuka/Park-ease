package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.repository.ParkingFacilityRepository;
import com.se1020.vehicleparking.repository.ParkingSlotRepository;
import com.se1020.vehicleparking.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParkingZoneService {

    private final ParkingZoneRepository zoneRepository;
    private final ParkingFacilityRepository facilityRepository;
    private final ParkingSlotRepository slotRepository;

    public ParkingZoneService(ParkingZoneRepository zoneRepository,
                              ParkingFacilityRepository facilityRepository,
                              ParkingSlotRepository slotRepository) {
        this.zoneRepository = zoneRepository;
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
    }

    public List<ParkingZone> getAllZones() { return zoneRepository.findAll(); }
    public List<ParkingZone> getZonesByFacility(String facilityId) { return zoneRepository.findByFacilityId(facilityId); }
    public ParkingZone getZoneById(String zoneId) { return zoneRepository.findById(zoneId); }

    public String addZone(String facilityId, String name, String floor, String description) {
        ParkingFacility facility = facilityRepository.findById(facilityId);
        if (facility == null) return "Please select a valid parking facility.";

        name = clean(name);
        floor = clean(floor);
        description = clean(description);
        if (name.isEmpty()) return "Zone name is required.";
        if (name.length() > 100) return "Zone name must be 100 characters or fewer.";
        if (floor.isEmpty()) return "Floor/location is required.";
        if (floor.length() > 60) return "Floor/location must be 60 characters or fewer.";
        if (description.length() > 300) return "Description must be 300 characters or fewer.";
        if (zoneRepository.existsByFacilityAndName(facilityId, name)) return "A zone with this name already exists in the selected facility.";

        zoneRepository.save(new ParkingZone(UUID.randomUUID().toString(), facilityId, name, floor, description, "ACTIVE"));
        return null;
    }

    public String updateZone(String zoneId, String facilityId, String name, String floor,
                             String description, String status) {
        ParkingZone zone = zoneRepository.findById(zoneId);
        if (zone == null) return "Parking zone not found.";
        if (facilityRepository.findById(facilityId) == null) return "Please select a valid parking facility.";

        name = clean(name);
        floor = clean(floor);
        description = clean(description);
        status = clean(status).toUpperCase();
        if (name.isEmpty()) return "Zone name is required.";
        if (name.length() > 100) return "Zone name must be 100 characters or fewer.";
        if (floor.isEmpty()) return "Floor/location is required.";
        if (floor.length() > 60) return "Floor/location must be 60 characters or fewer.";
        if (description.length() > 300) return "Description must be 300 characters or fewer.";
        if (!List.of("ACTIVE", "MAINTENANCE").contains(status)) return "Invalid zone status.";
        if (zoneRepository.existsByFacilityAndNameExcept(facilityId, name, zoneId)) return "A zone with this name already exists in the selected facility.";

        // Do not move a zone to another facility while it already contains slots; that would make
        // all child slot facility IDs inconsistent. It can be moved after its slots are moved/removed.
        if (!facilityId.equals(zone.getFacilityId()) && !slotRepository.findByZoneId(zoneId).isEmpty()) {
            return "A zone containing parking spaces cannot be moved to another facility.";
        }

        zone.setFacilityId(facilityId);
        zone.setName(name);
        zone.setFloor(floor);
        zone.setDescription(description);
        zone.setStatus(status);
        zoneRepository.update(zone);

        // Floor belongs to the zone. Keep existing child slots synchronized when only the floor
        // label changes within the same facility.
        for (var slot : slotRepository.findByZoneId(zoneId)) {
            slot.setFloor(floor);
            slotRepository.update(slot);
        }
        return null;
    }

    public String deleteZone(String zoneId) {
        ParkingZone zone = zoneRepository.findById(zoneId);
        if (zone == null) return "Parking zone not found.";
        if (!slotRepository.findByZoneId(zoneId).isEmpty()) {
            return "This zone still contains parking spaces. Remove or move them first.";
        }
        zoneRepository.delete(zoneId);
        return null;
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
