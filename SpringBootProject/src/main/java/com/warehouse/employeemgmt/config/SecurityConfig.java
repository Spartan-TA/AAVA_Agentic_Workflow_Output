package com.warehouse.employeemgmt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Spring Security configuration
 * 
 * Configures JWT-based authentication, role-based access control,
 * and stateless session management.
 * 
 * Features:
 * - JWT authentication
 * - Role-based access control (RBAC)
 * - Stateless session management
 * - Method-level security
 * - Public endpoints for health checks and API docs
 * - BCrypt password encoding
 * 
 * @author Warehouse Management Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configure security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}