package com.se1020.vehicleparking.repository;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thin facade kept so UserService/UserController do not need to change.
 * Backed by Spring Data JPA + SQL Server instead of JSON files.
 */
@Repository
public class UserRepository {

    @Autowired
    private UserJpaRepository userJpaRepository;

    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    public void saveAll(List<User> users) {
        userJpaRepository.saveAll(users);
    }

    public User findById(String userId) {
        return userJpaRepository.findById(userId).orElse(null);
    }

    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return userJpaRepository.findByEmailIgnoreCase(email.trim());
    }

    public void save(User user) {
        userJpaRepository.save(user);
    }

    public void update(User updatedUser) {
        userJpaRepository.save(updatedUser);
    }

    public void delete(String userId) {
        if (userJpaRepository.existsById(userId)) {
            userJpaRepository.deleteById(userId);
        }
    }
}
