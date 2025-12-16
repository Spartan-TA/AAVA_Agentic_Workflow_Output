package com.warehouse.employee.management.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for API key authentication.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String apiKeyValue;
    private static final String API_KEY_HEADER = "X-API-KEY";

    public ApiKeyAuthFilter(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey) && apiKey.equals(apiKeyValue)) {
            Authentication auth = new PreAuthenticatedAuthenticationToken(apiKey, null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (!request.getRequestURI().startsWith("/swagger-ui") && !request.getRequestURI().startsWith("/api-docs") && !request.getRequestURI().startsWith("/actuator")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
