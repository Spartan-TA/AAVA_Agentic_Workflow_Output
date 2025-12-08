package com.company.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for RBAC and JWT/API key authentication.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/api/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .antMatchers("/api/shifts/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/api/leaves/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .httpBasic(); // Replace with JWT filter for production
        return http.build();
    }
}
