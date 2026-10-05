package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.repository.jpa.ParkingFacilityJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParkingFacilityRepository {

    private final ParkingFacilityJpaRepository jpaRepository;

    public ParkingFacilityRepository(ParkingFacilityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public List<ParkingFacility> findAll() { return jpaRepository.findAll(); }
    public ParkingFacility findById(String id) { return jpaRepository.findById(id).orElse(null); }
    public void save(ParkingFacility facility) { jpaRepository.save(facility); }
    public void update(ParkingFacility facility) { jpaRepository.save(facility); }
    public void delete(String id) { if (jpaRepository.existsById(id)) jpaRepository.deleteById(id); }
    public boolean existsByName(String name) { return jpaRepository.existsByNameIgnoreCase(name); }
    public boolean existsByNameExcept(String name, String id) {
        return jpaRepository.existsByNameIgnoreCaseAndFacilityIdNot(name, id);
    }
}
