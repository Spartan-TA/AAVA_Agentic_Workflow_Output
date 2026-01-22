package com.warehouse.ems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for RBAC, OAuth2/API Key toggle.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.oauth2.enabled:true}")
    private boolean oauth2Enabled;
    @Value("${security.api-key.enabled:false}")
    private boolean apiKeyEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (oauth2Enabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
        } else if (apiKeyEnabled) {
            http.addFilter(new ApiKeyAuthFilter()); // Custom filter, to be implemented
        }
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/scheduling/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/leave/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/certification/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/safety/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/asset/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/review/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/payroll/**").hasRole("ADMIN")
                .requestMatchers("/notification/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/integration/**").hasRole("ADMIN")
                .requestMatchers("/audit/**").hasRole("ADMIN")
                .requestMatchers("/reporting/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/onboarding/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        return http.build();
    }

    /**
     * In-memory users for local testing. Replace with real user store in production.
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
