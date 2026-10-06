package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.ParkingZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingZoneJpaRepository extends JpaRepository<ParkingZone, String> {
    List<ParkingZone> findByFacilityId(String facilityId);
    boolean existsByFacilityIdAndNameIgnoreCase(String facilityId, String name);
    boolean existsByFacilityIdAndNameIgnoreCaseAndZoneIdNot(String facilityId, String name, String zoneId);
}
