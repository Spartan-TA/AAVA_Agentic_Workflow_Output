package com.warehouse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.io.IOException;

/**
 * Filter for API key authentication.
 */
public class ApiKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
    private final String headerName;
    private final String apiKeyValue;

    public ApiKeyAuthFilter(String headerName, String apiKeyValue) {
        this.headerName = headerName;
        this.apiKeyValue = apiKeyValue;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String apiKey = request.getHeader(headerName);
        if (apiKey == null || !apiKey.equals(apiKeyValue)) {
            throw new BadCredentialsException("Invalid API Key");
        }
        return apiKey;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "";
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }
}
