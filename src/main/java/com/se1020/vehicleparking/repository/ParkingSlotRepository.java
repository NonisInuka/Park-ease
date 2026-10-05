package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.repository.jpa.ParkingSlotJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParkingSlotRepository {

    @Autowired
    private ParkingSlotJpaRepository parkingSlotJpaRepository;

    public List<ParkingSlot> findAll() { return parkingSlotJpaRepository.findAll(); }
    public void saveAll(List<ParkingSlot> slots) { parkingSlotJpaRepository.saveAll(slots); }
    public ParkingSlot findById(String slotId) { return parkingSlotJpaRepository.findById(slotId).orElse(null); }
    public List<ParkingSlot> findByStatus(String status) { return parkingSlotJpaRepository.findByStatus(status); }
    public List<ParkingSlot> findByFacilityId(String facilityId) { return parkingSlotJpaRepository.findByFacilityId(facilityId); }
    public List<ParkingSlot> findByZoneId(String zoneId) { return parkingSlotJpaRepository.findByZoneId(zoneId); }
    public boolean existsByZoneAndSlotNumber(String zoneId, String slotNumber) {
        return parkingSlotJpaRepository.existsByZoneIdAndSlotNumberIgnoreCase(zoneId, slotNumber);
    }
    public boolean existsByZoneAndSlotNumberExcept(String zoneId, String slotNumber, String slotId) {
        return parkingSlotJpaRepository.existsByZoneIdAndSlotNumberIgnoreCaseAndSlotIdNot(zoneId, slotNumber, slotId);
    }
    public void save(ParkingSlot slot) { parkingSlotJpaRepository.save(slot); }
    public void update(ParkingSlot slot) { parkingSlotJpaRepository.save(slot); }
    public void delete(String slotId) { if (parkingSlotJpaRepository.existsById(slotId)) parkingSlotJpaRepository.deleteById(slotId); }
}
