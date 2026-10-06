package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_zones")
public class ParkingZone {

    @Id
    @Column(name = "zone_id", length = 64)
    private String zoneId;

    @Column(name = "facility_id", nullable = false, length = 64)
    private String facilityId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 60)
    private String floor;

    @Column(length = 300)
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, MAINTENANCE

    public ParkingZone() {}

    public ParkingZone(String zoneId, String facilityId, String name, String floor,
                       String description, String status) {
        this.zoneId = zoneId;
        this.facilityId = facilityId;
        this.name = name;
        this.floor = floor;
        this.description = description;
        this.status = status;
    }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
