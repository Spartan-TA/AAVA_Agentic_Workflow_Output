package com.warehouse.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Simple API Key authentication filter for local/dev environments.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String apiKeyValue;
    private static final String HEADER_NAME = "X-API-KEY";

    public ApiKeyAuthFilter(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(apiKey) && apiKey.equals(apiKeyValue)) {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "apiKeyUser",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            ((UsernamePasswordAuthenticationToken) auth).setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
