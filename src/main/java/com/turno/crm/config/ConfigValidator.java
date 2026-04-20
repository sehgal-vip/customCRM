package com.turno.crm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigValidator {

    @Value("${turno.jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void validate() {
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            if (jwtSecret == null || jwtSecret.isBlank()) {
                throw new IllegalStateException("JWT_SECRET must be set in production");
            }
            if (jwtSecret.length() < 32) {
                throw new IllegalStateException("JWT_SECRET must be at least 256 bits (32 bytes)");
            }
        }
    }
}
