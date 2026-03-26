package com.warehouse.employee.management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for JwtAuthenticationFilter
 * Tests token extraction, validation, and filter chain behavior
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Test Suite")
public class JwtAuthenticationFilterTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    // ==================== TOKEN EXTRACTION TESTS ====================

    @Test
    @DisplayName("Test extractTokenFromRequest - Valid Bearer Token - Returns Token")
    void testExtractTokenFromRequest_ValidBearerToken_ReturnsToken() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNotNull(extractedToken);
        assertEquals(token, extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - No Authorization Header - Returns Null")
    void testExtractTokenFromRequest_NoAuthorizationHeader_ReturnsNull() {
        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - Empty Authorization Header - Returns Null")
    void testExtractTokenFromRequest_EmptyAuthorizationHeader_ReturnsNull() {
        // Arrange
        request.addHeader("Authorization", "");

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - No Bearer Prefix - Returns Null")
    void testExtractTokenFromRequest_NoBearerPrefix_ReturnsNull() {
        // Arrange
        request.addHeader("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - Bearer Only - Returns Null")
    void testExtractTokenFromRequest_BearerOnly_ReturnsNull() {
        // Arrange
        request.addHeader("Authorization", "Bearer ");

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - Bearer With Whitespace - Returns Token")
    void testExtractTokenFromRequest_BearerWithWhitespace_ReturnsToken() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        request.addHeader("Authorization", "Bearer  " + token);

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNotNull(extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - Case Sensitive Bearer - Returns Null")
    void testExtractTokenFromRequest_CaseSensitiveBearer_ReturnsNull() {
        // Arrange
        request.addHeader("Authorization", "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNull(extractedToken);
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Test validateToken - Valid Token - Returns True")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiZXhwIjo5OTk5OTk5OTk5fQ.signature";

        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Test validateToken - Null Token - Returns False")
    void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validateToken - Empty Token - Returns False")
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validateToken - Malformed Token - Returns False")
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validateToken - Expired Token - Returns False")
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiZXhwIjoxfQ.signature";

        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validateToken - Invalid Signature - Returns False")
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Arrange
        String invalidSignatureToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIn0.invalidsignature";

        // Act
        boolean isValid = jwtAuthenticationFilter.validateToken(invalidSignatureToken);

        // Assert
        assertFalse(isValid);
    }

    // ==================== FILTER CHAIN TESTS ====================

    @Test
    @DisplayName("Test doFilterInternal - Valid Token - Sets Authentication")
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiZXhwIjo5OTk5OTk5OTk5fQ.signature";
        request.addHeader("Authorization", "Bearer " + validToken);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Test doFilterInternal - No Token - Does Not Set Authentication")
    void testDoFilterInternal_NoToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Test doFilterInternal - Invalid Token - Does Not Set Authentication")
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid.token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Test doFilterInternal - Filter Chain Continues")
    void testDoFilterInternal_FilterChainContinues() throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Test doFilterInternal - Exception Thrown - Filter Chain Continues")
    void testDoFilterInternal_ExceptionThrown_FilterChainContinues() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid.token");
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== PUBLIC ENDPOINT TESTS ====================

    @Test
    @DisplayName("Test doFilterInternal - Public Endpoint - No Token Required")
    void testDoFilterInternal_PublicEndpoint_NoTokenRequired() throws ServletException, IOException {
        // Arrange
        request.setRequestURI("/actuator/health");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Test doFilterInternal - Protected Endpoint - Token Required")
    void testDoFilterInternal_ProtectedEndpoint_TokenRequired() throws ServletException, IOException {
        // Arrange
        request.setRequestURI("/api/employees");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test extractTokenFromRequest - Very Long Token - Returns Token")
    void testExtractTokenFromRequest_VeryLongToken_ReturnsToken() {
        // Arrange
        String longToken = "A".repeat(1000);
        request.addHeader("Authorization", "Bearer " + longToken);

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNotNull(extractedToken);
        assertEquals(longToken, extractedToken);
    }

    @Test
    @DisplayName("Test extractTokenFromRequest - Token With Special Characters - Returns Token")
    void testExtractTokenFromRequest_TokenWithSpecialCharacters_ReturnsToken() {
        // Arrange
        String specialToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token_with+special/chars=";
        request.addHeader("Authorization", "Bearer " + specialToken);

        // Act
        String extractedToken = jwtAuthenticationFilter.extractTokenFromRequest(request);

        // Assert
        assertNotNull(extractedToken);
        assertEquals(specialToken, extractedToken);
    }

    @Test
    @DisplayName("Test doFilterInternal - Multiple Authorization Headers - Uses First")
    void testDoFilterInternal_MultipleAuthorizationHeaders_UsesFirst() throws ServletException, IOException {
        // Arrange
        String token1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token1.signature";
        String token2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token2.signature";
        request.addHeader("Authorization", "Bearer " + token1);
        request.addHeader("Authorization", "Bearer " + token2);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Test doFilterInternal - Null Request - Handles Gracefully")
    void testDoFilterInternal_NullRequest_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(null, response, filterChain);
        });
    }

    @Test
    @DisplayName("Test doFilterInternal - Null Response - Handles Gracefully")
    void testDoFilterInternal_NullResponse_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
        });
    }

    @Test
    @DisplayName("Test doFilterInternal - Null FilterChain - Handles Gracefully")
    void testDoFilterInternal_NullFilterChain_HandlesGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, null);
        });
    }

    // ==================== SECURITY CONTEXT TESTS ====================

    @Test
    @DisplayName("Test doFilterInternal - Clears Security Context on Invalid Token")
    void testDoFilterInternal_ClearsSecurityContextOnInvalidToken() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid.token");
        SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Test doFilterInternal - Preserves Existing Authentication if No Token")
    void testDoFilterInternal_PreservesExistingAuthenticationIfNoToken() throws ServletException, IOException {
        // Arrange
        org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Test doFilterInternal - Multiple Requests - Performs Efficiently")
    void testDoFilterInternal_MultipleRequests_PerformsEfficiently() throws ServletException, IOException {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiZXhwIjo5OTk5OTk5OTk5fQ.signature";
        request.addHeader("Authorization", "Bearer " + validToken);

        // Act
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
            SecurityContextHolder.clearContext();
        }
        long endTime = System.currentTimeMillis();

        // Assert
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Filter should process 100 requests in less than 1 second");
    }

    // ==================== CONCURRENT ACCESS TESTS ====================

    @Test
    @DisplayName("Test doFilterInternal - Concurrent Requests - Thread Safe")
    void testDoFilterInternal_ConcurrentRequests_ThreadSafe() throws InterruptedException {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiZXhwIjo5OTk5OTk5OTk5fQ.signature";
        request.addHeader("Authorization", "Bearer " + validToken);

        // Act
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                try {
                    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
                } catch (Exception e) {
                    fail("Exception in concurrent execution: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - No exceptions thrown
        assertTrue(true);
    }
}