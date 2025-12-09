package com.warehouseems.security.config;

import org.springframework.beans.factory.annotation.Value;
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
 * Security configuration for RBAC, endpoint/method security, and API key/OAuth2 toggle.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${security.apikey.enabled:false}")
    private boolean apiKeyEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (apiKeyEnabled) {
            // API Key authentication (example, should be implemented)
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .anyRequest().authenticated()
            ).httpBasic();
        } else {
            // OAuth2 authentication (example, should be implemented)
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                    .anyRequest().authenticated()
            ).oauth2Login();
        }
        http.csrf().disable();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService bean should be implemented to load users from DB
}
