package com.company.warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA auditing.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
}