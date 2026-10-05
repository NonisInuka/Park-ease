package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @Column(name = "slot_id", length = 64)
    private String slotId;

    private String slotNumber;
    private String floor;

    // Kept as the original column name for backward compatibility. It now consistently means
    // the vehicle category the bay is designed for (see VehicleCategories).
    private String type;

    @Column(name = "space_category", length = 30)
    private String spaceCategory; // STANDARD, ACCESSIBLE, PREMIUM

    @Column(name = "facility_id", length = 64)
    private String facilityId;

    @Column(name = "zone_id", length = 64)
    private String zoneId;

    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private double ratePerHour;

    public ParkingSlot() {}

    // Legacy constructor retained so older seed/deserialization code remains compatible.
    public ParkingSlot(String slotId, String slotNumber, String floor,
                       String type, String status, double ratePerHour) {
        this(slotId, slotNumber, floor, type, "STANDARD", null, null, status, ratePerHour);
    }

    public ParkingSlot(String slotId, String slotNumber, String floor, String type,
                       String spaceCategory, String facilityId, String zoneId,
                       String status, double ratePerHour) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.floor = floor;
        this.type = type;
        this.spaceCategory = spaceCategory;
        this.facilityId = facilityId;
        this.zoneId = zoneId;
        this.status = status;
        this.ratePerHour = ratePerHour;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSpaceCategory() { return spaceCategory; }
    public void setSpaceCategory(String spaceCategory) { this.spaceCategory = spaceCategory; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(double ratePerHour) { this.ratePerHour = ratePerHour; }
}
