package com.warehouse.ems.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;

/**
 * Observability configuration for Prometheus metrics and distributed tracing (Zipkin).
 */
@Configuration
public class ObservabilityConfig {
    private static final Logger log = LoggerFactory.getLogger(ObservabilityConfig.class);

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "warehouse-ems");
    }

    // Zipkin is auto-configured via properties (spring.zipkin.*)
}
