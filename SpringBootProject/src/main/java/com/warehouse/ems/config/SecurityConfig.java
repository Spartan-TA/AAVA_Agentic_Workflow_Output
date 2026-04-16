package com.warehouse.ems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import javax.servlet.http.HttpServletResponse;

/**
 * Security configuration for RBAC and authentication mode toggle.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode:apikey}")
    private String securityMode;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(securityMode)) {
            http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic(); // Placeholder for API key filter
        } else {
            // OAuth2 configuration placeholder
            http.oauth2Login();
        }
        http.exceptionHandling()
            .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN));
    }
}
