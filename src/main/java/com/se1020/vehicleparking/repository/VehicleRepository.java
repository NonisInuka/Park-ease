package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.Vehicle;
import com.se1020.vehicleparking.repository.jpa.VehicleJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so VehicleService/VehicleController do not need to change.
 * Backed by Spring Data JPA + SQL Server instead of JSON files.
 */
@Repository
public class VehicleRepository {

    @Autowired
    private VehicleJpaRepository vehicleJpaRepository;

    public List<Vehicle> findAll() {
        return vehicleJpaRepository.findAll();
    }

    public void saveAll(List<Vehicle> vehicles) {
        vehicleJpaRepository.saveAll(vehicles);
    }

    public Vehicle findById(String vehicleId) {
        return vehicleJpaRepository.findById(vehicleId).orElse(null);
    }

    public List<Vehicle> findByOwnerId(String ownerId) {
        return vehicleJpaRepository.findByOwnerId(ownerId);
    }

    public void save(Vehicle vehicle) {
        vehicleJpaRepository.save(vehicle);
    }

    public void update(Vehicle updatedVehicle) {
        vehicleJpaRepository.save(updatedVehicle);
    }

    public void delete(String vehicleId) {
        if (vehicleJpaRepository.existsById(vehicleId)) {
            vehicleJpaRepository.deleteById(vehicleId);
        }
    }
}
