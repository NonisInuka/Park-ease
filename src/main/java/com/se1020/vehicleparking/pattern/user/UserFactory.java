package com.se1020.vehicleparking.pattern.user;

import com.se1020.vehicleparking.model.AdminUser;
import com.se1020.vehicleparking.model.RegularUser;
import com.se1020.vehicleparking.model.User;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern for creating User products.
 *
 * User is the common product abstraction. RegularUser and AdminUser are concrete products.
 * The client (UserService) asks this factory for the required user type instead of directly
 * constructing concrete subclasses.
 */
@Component
public class UserFactory {

    public static final String REGULAR_USER = "USER";
    public static final String ADMIN_USER = "ADMIN";

    public User createUser(String userType,
                           String userId,
                           String name,
                           String email,
                           String password,
                           String phone,
                           String roleSpecificValue,
                           String stakeholderRole) {

        if (REGULAR_USER.equalsIgnoreCase(userType)) {
            return new RegularUser(
                    userId,
                    name,
                    email,
                    password,
                    phone,
                    roleSpecificValue,
                    stakeholderRole
            );
        }

        if (ADMIN_USER.equalsIgnoreCase(userType)) {
            return new AdminUser(
                    userId,
                    name,
                    email,
                    password,
                    phone,
                    roleSpecificValue,
                    stakeholderRole
            );
        }

        throw new IllegalArgumentException("Unsupported user type: " + userType);
    }
}
