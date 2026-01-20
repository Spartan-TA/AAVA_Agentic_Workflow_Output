package com.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import java.util.Optional;

/**
 * Audit configuration for immutable logging of sensitive changes.
 * Provides current user context for audit entries.
 */
@Configuration
public class AuditConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        // In production, fetch from security context
        return () -> Optional.ofNullable("system");
    }
}
