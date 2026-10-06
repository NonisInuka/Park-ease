package com.se1020.vehicleparking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Only spring-security-crypto (the standalone BCrypt utility jar) is used here - not the
 * spring-boot-starter-security starter. No filter chain, no auto-configured login page, and no
 * change to the existing session-based loggedUser authentication flow.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
