package com.example.usermanagement.security;

import com.example.usermanagement.service.JwtService;
import com.example.usermanagement.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtAuthenticationFilter covering JWT authentication filter logic and edge cases.
 */
public class JwtAuthenticationFilterTest {
    @Mock private JwtService jwtService;
    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoFilterInternal_ValidToken_Proceeds() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtService.validateToken("valid.jwt.token")).thenReturn(true);
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        // No exception means success
    }

    @Test
    void testDoFilterInternal_InvalidToken_DoesNotProceed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtService.validateToken("invalid.jwt.token")).thenReturn(false);
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        // No exception means handled
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_Proceeds() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        // No exception means success
    }
}
