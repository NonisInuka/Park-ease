package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSlotJpaRepository extends JpaRepository<ParkingSlot, String> {
    List<ParkingSlot> findByStatus(String status);
    List<ParkingSlot> findByFacilityId(String facilityId);
    List<ParkingSlot> findByZoneId(String zoneId);
    boolean existsByZoneIdAndSlotNumberIgnoreCase(String zoneId, String slotNumber);
    boolean existsByZoneIdAndSlotNumberIgnoreCaseAndSlotIdNot(String zoneId, String slotNumber, String slotId);
}
