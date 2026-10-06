package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.repository.jpa.ParkingZoneJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParkingZoneRepository {

    private final ParkingZoneJpaRepository jpaRepository;

    public ParkingZoneRepository(ParkingZoneJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public List<ParkingZone> findAll() { return jpaRepository.findAll(); }
    public ParkingZone findById(String id) { return jpaRepository.findById(id).orElse(null); }
    public List<ParkingZone> findByFacilityId(String facilityId) { return jpaRepository.findByFacilityId(facilityId); }
    public void save(ParkingZone zone) { jpaRepository.save(zone); }
    public void update(ParkingZone zone) { jpaRepository.save(zone); }
    public void delete(String id) { if (jpaRepository.existsById(id)) jpaRepository.deleteById(id); }
    public boolean existsByFacilityAndName(String facilityId, String name) {
        return jpaRepository.existsByFacilityIdAndNameIgnoreCase(facilityId, name);
    }
    public boolean existsByFacilityAndNameExcept(String facilityId, String name, String zoneId) {
        return jpaRepository.existsByFacilityIdAndNameIgnoreCaseAndZoneIdNot(facilityId, name, zoneId);
    }
}
