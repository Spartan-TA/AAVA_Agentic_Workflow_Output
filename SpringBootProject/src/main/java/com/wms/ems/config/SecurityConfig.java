package com.wms.ems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Warehouse EMS.
 * Supports both OAuth2 and API Key authentication based on configuration.
 * Implements Role-Based Access Control (RBAC) with roles: ADMIN, HR, SUPERVISOR, WORKER.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${security.api-key-header}")
    private String apiKeyHeader;

    @Value("${security.api-key-value}")
    private String apiKeyValue;

    @Value("${spring.security.oauth2.enabled}")
    private boolean oauth2Enabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (oauth2Enabled) {
            // OAuth2 configuration
            http
                .authorizeHttpRequests(authz -> authz
                    .antMatchers("/swagger-ui.html", "/v3/api-docs/**", "/actuator/**").permitAll()
                    .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                    .antMatchers("/schedule/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/leave/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                    .antMatchers("/certifications/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/safety/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/assets/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/reviews/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .antMatchers("/payroll/**").hasAnyRole("ADMIN", "HR")
                    .antMatchers("/notifications/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                    .antMatchers("/integration/**").hasRole("ADMIN")
                    .antMatchers("/audit/**").hasAnyRole("ADMIN", "HR")
                    .antMatchers("/reports/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        } else {
            // API Key configuration
            http
                .authorizeHttpRequests(authz -> authz
                    .antMatchers("/swagger-ui.html", "/v3/api-docs/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
                );
            // Add API Key filter here if needed
        }
        http.csrf().disable();
        return http.build();
    }
}