package com.se1020.vehicleparking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.io.Serializable;

// NOTE: "user_type" is a JPA-only discriminator column used solely to let Hibernate pick the
// correct subclass (AdminUser/RegularUser) when loading rows. The existing "role" column/field
// is untouched and keeps driving all existing application logic (getDashboardPath(), templates, etc).
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING, length = 20)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdminUser.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = RegularUser.class, name = "USER")
})
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String phone;
    private String role;

    // Phase 2 additions. Both default so existing rows created before this migration are
    // backfilled safely by DataSeeder's startup migration pass rather than being left null.
    @Column(length = 20)
    private String status = "ACTIVE";

    // See UserRoles.java for the full explanation of why this is separate from "role".
    @Column(length = 40)
    private String stakeholderRole;

    public User() {}

    public User(String userId, String name, String email, String password, String phone, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStakeholderRole() { return stakeholderRole; }
    public void setStakeholderRole(String stakeholderRole) { this.stakeholderRole = stakeholderRole; }

    @JsonIgnore
    public boolean isActive() {
        return status == null || "ACTIVE".equalsIgnoreCase(status);
    }

    @JsonIgnore
    public abstract String getDashboardPath();
}