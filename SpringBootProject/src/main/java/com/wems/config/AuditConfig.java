package com.wems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Audit configuration for Warehouse EMS.
 * Enables JPA auditing for tracking entity changes.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Provides the current auditor (user) for auditing purposes.
     * In production, integrate with Spring Security to fetch the logged-in user.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // For demonstration, returns 'system'. Replace with actual user from security context.
        return () -> Optional.ofNullable("system");
    }
}
