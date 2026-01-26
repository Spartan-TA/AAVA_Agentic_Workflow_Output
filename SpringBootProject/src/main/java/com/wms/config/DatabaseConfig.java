package com.wms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
    // Additional beans for multi-tenancy or custom JPA configuration can be added here
}
