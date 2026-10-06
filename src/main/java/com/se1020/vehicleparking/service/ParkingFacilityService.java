package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.repository.ParkingFacilityRepository;
import com.se1020.vehicleparking.repository.ParkingSlotRepository;
import com.se1020.vehicleparking.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParkingFacilityService {

    private final ParkingFacilityRepository facilityRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingSlotRepository slotRepository;

    public ParkingFacilityService(ParkingFacilityRepository facilityRepository,
                                  ParkingZoneRepository zoneRepository,
                                  ParkingSlotRepository slotRepository) {
        this.facilityRepository = facilityRepository;
        this.zoneRepository = zoneRepository;
        this.slotRepository = slotRepository;
    }

    public List<ParkingFacility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public ParkingFacility getFacilityById(String facilityId) {
        return facilityRepository.findById(facilityId);
    }

    public String addFacility(String name, String address, String description) {
        name = clean(name);
        address = clean(address);
        description = clean(description);

        if (name.isEmpty()) return "Facility name is required.";
        if (name.length() > 120) return "Facility name must be 120 characters or fewer.";
        if (address.isEmpty()) return "Facility address is required.";
        if (address.length() > 255) return "Facility address must be 255 characters or fewer.";
        if (description.length() > 500) return "Description must be 500 characters or fewer.";
        if (facilityRepository.existsByName(name)) return "A parking facility with this name already exists.";

        facilityRepository.save(new ParkingFacility(UUID.randomUUID().toString(), name, address, description, "ACTIVE"));
        return null;
    }

    public String updateFacility(String facilityId, String name, String address, String description, String status) {
        ParkingFacility facility = facilityRepository.findById(facilityId);
        if (facility == null) return "Parking facility not found.";

        name = clean(name);
        address = clean(address);
        description = clean(description);
        status = clean(status).toUpperCase();

        if (name.isEmpty()) return "Facility name is required.";
        if (name.length() > 120) return "Facility name must be 120 characters or fewer.";
        if (address.isEmpty()) return "Facility address is required.";
        if (address.length() > 255) return "Facility address must be 255 characters or fewer.";
        if (description.length() > 500) return "Description must be 500 characters or fewer.";
        if (!List.of("ACTIVE", "INACTIVE").contains(status)) return "Invalid facility status.";
        if (facilityRepository.existsByNameExcept(name, facilityId)) return "A parking facility with this name already exists.";

        facility.setName(name);
        facility.setAddress(address);
        facility.setDescription(description);
        facility.setStatus(status);
        facilityRepository.update(facility);
        return null;
    }

    public String deleteFacility(String facilityId) {
        ParkingFacility facility = facilityRepository.findById(facilityId);
        if (facility == null) return "Parking facility not found.";
        if (!zoneRepository.findByFacilityId(facilityId).isEmpty() || !slotRepository.findByFacilityId(facilityId).isEmpty()) {
            return "This facility still contains zones or parking spaces. Remove or move them first.";
        }
        facilityRepository.delete(facilityId);
        return null;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
