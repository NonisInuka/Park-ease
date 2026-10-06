package com.se1020.vehicleparking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class AdminUser extends User {

    private String adminCode;

    public AdminUser() {
        super();
    }

    public AdminUser(String userId, String name, String email, String password, String phone, String adminCode) {
        this(userId, name, email, password, phone, adminCode, UserRoles.ADMINISTRATOR);
    }

    public AdminUser(String userId, String name, String email, String password, String phone,
                      String adminCode, String stakeholderRole) {
        super(userId, name, email, password, phone, "ADMIN");
        this.adminCode = adminCode;
        setStakeholderRole(UserRoles.isValidAdminTierRole(stakeholderRole) ? stakeholderRole : UserRoles.ADMINISTRATOR);
    }

    public String getAdminCode() { return adminCode; }
    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }

    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }
}