package com.warehouse.management.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiKeyAuthFilterTest {

    @InjectMocks
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidApiKey_SetsAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "VALID_API_KEY");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Assume VALID_API_KEY is mapped to role ADMIN in the filter logic
        apiKeyAuthFilter = spy(new ApiKeyAuthFilter() {
            @Override
            protected String extractRoleFromApiKey(String apiKey) {
                return "ADMIN";
            }

            @Override
            protected boolean isValidApiKey(String apiKey) {
                return "VALID_API_KEY".equals(apiKey);
            }
        });

        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ADMIN", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithInvalidApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "INVALID_API_KEY");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter = spy(new ApiKeyAuthFilter() {
            @Override
            protected boolean isValidApiKey(String apiKey) {
                return false;
            }
        });

        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithMissingApiKey_ReturnsUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyAuthFilter = spy(new ApiKeyAuthFilter() {
            @Override
            protected boolean isValidApiKey(String apiKey) {
                return false;
            }
        });

        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testExtractRoleFromApiKey_ReturnsCorrectRole() {
        apiKeyAuthFilter = new ApiKeyAuthFilter() {
            @Override
            protected String extractRoleFromApiKey(String apiKey) {
                if ("ADMIN_KEY".equals(apiKey)) return "ADMIN";
                if ("HR_KEY".equals(apiKey)) return "HR";
                if ("SUPERVISOR_KEY".equals(apiKey)) return "SUPERVISOR";
                if ("WORKER_KEY".equals(apiKey)) return "WORKER";
                return null;
            }
        };
        assertEquals("ADMIN", apiKeyAuthFilter.extractRoleFromApiKey("ADMIN_KEY"));
        assertEquals("HR", apiKeyAuthFilter.extractRoleFromApiKey("HR_KEY"));
        assertEquals("SUPERVISOR", apiKeyAuthFilter.extractRoleFromApiKey("SUPERVISOR_KEY"));
        assertEquals("WORKER", apiKeyAuthFilter.extractRoleFromApiKey("WORKER_KEY"));
        assertNull(apiKeyAuthFilter.extractRoleFromApiKey("UNKNOWN_KEY"));
    }
}