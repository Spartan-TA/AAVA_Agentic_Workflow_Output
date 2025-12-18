package com.warehouse.ems.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for RBAC and endpoint protection.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }

    /**
     * In-memory users for demo/testing. Replace with real user store in production.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password("{noop}adminpass").roles("ADMIN").build(),
            User.withUsername("hr").password("{noop}hrpass").roles("HR").build(),
            User.withUsername("supervisor").password("{noop}supervisorpass").roles("SUPERVISOR").build(),
            User.withUsername("worker").password("{noop}workerpass").roles("WORKER").build()
        );
    }
}
