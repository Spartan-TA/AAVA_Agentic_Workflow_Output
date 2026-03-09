package com.company.warehouse.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Actuator configuration for custom health and metrics endpoints.
 */
@Configuration
public class ActuatorConfig {

    @Bean
    public WebEndpointProperties webEndpointProperties() {
        WebEndpointProperties properties = new WebEndpointProperties();
        properties.setBasePath("/actuator");
        return properties;
    }

    // Additional actuator beans can be configured here
}
