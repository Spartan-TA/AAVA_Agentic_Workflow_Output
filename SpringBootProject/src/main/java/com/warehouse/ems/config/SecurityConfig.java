package com.warehouse.ems.config;

import com.warehouse.ems.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/shifts/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/leave/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/certifications/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/safety/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/assets/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/reviews/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/payroll/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/api/v1/notifications/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            )
            .addFilter(new JwtAuthenticationFilter())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}