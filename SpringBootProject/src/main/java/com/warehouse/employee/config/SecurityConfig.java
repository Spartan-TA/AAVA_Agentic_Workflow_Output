package com.warehouse.employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for role-based access control and method-level security.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }

    /**
     * In-memory user details for demonstration. Replace with persistent user store in production.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build(),
            User.withUsername("hr").password("{noop}hr123").roles("HR").build(),
            User.withUsername("supervisor").password("{noop}supervisor123").roles("SUPERVISOR").build(),
            User.withUsername("worker").password("{noop}worker123").roles("WORKER").build()
        );
    }
}
