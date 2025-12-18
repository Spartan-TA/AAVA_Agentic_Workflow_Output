package com.warehouse.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;
import java.util.Optional;

/**
 * JPA/Hibernate configuration for Warehouse EMS.
 * Enables auditing and configures AuditorAware bean.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * Provides current auditor (username) for audit fields.
     * @return AuditorAware<String>
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // In production, fetch from SecurityContextHolder
        return () -> Optional.ofNullable("system");
    }
}
