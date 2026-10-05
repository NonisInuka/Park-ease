package com.se1020.vehicleparking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("USER")
public class RegularUser extends User {

    private String licenseNumber;

    public RegularUser() {
        super();
    }

    public RegularUser(String userId, String name, String email, String password, String phone, String licenseNumber) {
        this(userId, name, email, password, phone, licenseNumber, UserRoles.DRIVER);
    }

    public RegularUser(String userId, String name, String email, String password, String phone,
                        String licenseNumber, String stakeholderRole) {
        super(userId, name, email, password, phone, "USER");
        this.licenseNumber = licenseNumber;
        setStakeholderRole(UserRoles.isValidUserTierRole(stakeholderRole) ? stakeholderRole : UserRoles.DRIVER);
    }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    @Override
    public String getDashboardPath() {
        return "/user/dashboard";
    }
}