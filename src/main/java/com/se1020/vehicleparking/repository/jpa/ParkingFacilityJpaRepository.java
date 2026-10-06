package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.ParkingFacility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingFacilityJpaRepository extends JpaRepository<ParkingFacility, String> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndFacilityIdNot(String name, String facilityId);
}
