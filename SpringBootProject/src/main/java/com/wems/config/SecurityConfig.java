package com.wems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Warehouse EMS.
 * Implements RBAC with JWT/OAuth2 and method-level security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Password encoder bean using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain configuration.
     * Configures endpoints, JWT, and role-based access.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/hr/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/api/supervisor/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/api/worker/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            // JWT/OAuth2 configuration placeholder
            .oauth2ResourceServer().jwt();
        return http.build();
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }
}
