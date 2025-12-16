package com.warehouse.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Warehouse Employee Management System.
 * Implements role-based access control (RBAC) with support for API key and OAuth2 authentication.
 * 
 * Roles:
 * - ADMIN: Full system access
 * - HR: Employee management and reporting
 * - SUPERVISOR: Team management and scheduling
 * - WORKER: Self-service access to schedules and time tracking
 * 
 * @author Warehouse Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Value("${security.mode:basic}")
    private String securityMode;

    /**
     * Configure HTTP security with role-based access control.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Employee endpoints - require authentication
                .requestMatchers("/api/v1/employees/**").authenticated()
                
                // Attendance endpoints - require authentication
                .requestMatchers("/api/v1/attendance/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        // Configure authentication based on security mode
        if ("oauth2".equalsIgnoreCase(securityMode)) {
            // OAuth2 Resource Server configuration
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        } else {
            // Basic authentication (default)
            http.httpBasic(basic -> {});
        }

        return http.build();
    }

    /**
     * Password encoder bean for secure password hashing.
     * Uses BCrypt algorithm with strength 12.
     * 
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * In-memory user details service for development and testing.
     * In production, this should be replaced with a database-backed implementation.
     * 
     * @return UserDetailsService with predefined users
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Admin user with full access
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        // HR user with employee management access
        UserDetails hr = User.builder()
            .username("hr")
            .password(passwordEncoder().encode("hr123"))
            .roles("HR")
            .build();

        // Supervisor user with team management access
        UserDetails supervisor = User.builder()
            .username("supervisor")
            .password(passwordEncoder().encode("supervisor123"))
            .roles("SUPERVISOR")
            .build();

        // Worker user with self-service access
        UserDetails worker = User.builder()
            .username("worker")
            .password(passwordEncoder().encode("worker123"))
            .roles("WORKER")
            .build();

        return new InMemoryUserDetailsManager(admin, hr, supervisor, worker);
    }
}