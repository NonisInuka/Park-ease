package com.se1020.vehicleparking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_facilities")
public class ParkingFacility {

    @Id
    @Column(name = "facility_id", length = 64)
    private String facilityId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE

    public ParkingFacility() {}

    public ParkingFacility(String facilityId, String name, String address, String description, String status) {
        this.facilityId = facilityId;
        this.name = name;
        this.address = address;
        this.description = description;
        this.status = status;
    }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
