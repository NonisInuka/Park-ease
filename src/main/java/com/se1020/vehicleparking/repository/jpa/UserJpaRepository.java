package com.se1020.vehicleparking.repository.jpa;

import com.se1020.vehicleparking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, String> {
    User findByEmailIgnoreCase(String email);
}
