package com.warehouse.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for RBAC, JWT/OAuth2, and endpoint/method security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/hr/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/api/supervisor/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/worker/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, excep) -> res.sendError(401, "Unauthorized"))
                .accessDeniedHandler((req, res, excep) -> res.sendError(403, "Forbidden"))
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService bean can be customized for row-level security, API key, etc.
    @Bean
    public UserDetailsService userDetailsService() {
        // TODO: Implement custom UserDetailsService for RBAC and row-level constraints
        return username -> null;
    }
}
