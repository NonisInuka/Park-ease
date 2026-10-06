package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleJpaRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByOwnerId(String ownerId);
}
