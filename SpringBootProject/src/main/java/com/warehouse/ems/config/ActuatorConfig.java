package com.warehouse.ems.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Configuration for Spring Boot Actuator endpoints.
 * Restricts access to actuator endpoints to ADMIN role.
 */
@Configuration
public class ActuatorConfig {

    @Bean
    public WebEndpointProperties webEndpointProperties() {
        WebEndpointProperties properties = new WebEndpointProperties();
        properties.setBasePath("/actuator");
        return properties;
    }

    public void configureActuatorSecurity(HttpSecurity http) throws Exception {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests()
            .anyRequest().hasRole("ADMIN");
    }
}
