package com.company.warehouse.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtTokenProvider.
 * Tests cover token generation, validation, expiration, and security edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret = "testSecretKeyForJWTTokenGenerationAndValidation123456789";
    private long jwtExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);
        jwtTokenProvider.init();
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    @DisplayName("Should generate valid JWT token for authenticated user")
    void testGenerateToken_ValidAuthentication_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate token with correct username")
    void testGenerateToken_CorrectUsername_Success() {
        // Arrange
        String username = "john.doe@company.com";
        Authentication authentication = createMockAuthentication(username, "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should generate token with expiration time")
    void testGenerateToken_HasExpirationTime_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should throw exception when generating token with null authentication")
    void testGenerateToken_NullAuthentication_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateToken(null);
        });
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Arrange
        Authentication auth1 = createMockAuthentication("user1@company.com", "ROLE_WORKER");
        Authentication auth2 = createMockAuthentication("user2@company.com", "ROLE_ADMIN");

        // Act
        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth2);

        // Assert
        assertNotEquals(token1, token2);
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    @DisplayName("Should validate correct JWT token")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Arrange
        String tokenWithInvalidSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalidSignature";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithInvalidSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed JWT token")
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject expired token")
    void testValidateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        // Arrange
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpiration", 1L); // 1 millisecond
        shortExpirationProvider.init();
        
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = shortExpirationProvider.generateToken(authentication);
        
        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpirationProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    // ========== USERNAME EXTRACTION TESTS ==========

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromToken_ValidToken_Success() {
        // Arrange
        String username = "john.doe@company.com";
        Authentication authentication = createMockAuthentication(username, "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should throw exception when extracting username from invalid token")
    void testGetUsernameFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    @DisplayName("Should throw exception when extracting username from null token")
    void testGetUsernameFromToken_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    @Test
    @DisplayName("Should extract username with special characters")
    void testGetUsernameFromToken_SpecialCharacters_Success() {
        // Arrange
        String username = "john.o'brien+test@company.com";
        Authentication authentication = createMockAuthentication(username, "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    // ========== EXPIRATION DATE TESTS ==========

    @Test
    @DisplayName("Should extract expiration date from valid token")
    void testGetExpirationDateFromToken_ValidToken_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should calculate correct expiration time")
    void testGetExpirationDateFromToken_CorrectExpiration_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtTokenProvider.generateToken(authentication);
        long afterGeneration = System.currentTimeMillis();
        
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
        long expirationTime = expirationDate.getTime();

        // Act & Assert
        assertTrue(expirationTime >= beforeGeneration + jwtExpiration);
        assertTrue(expirationTime <= afterGeneration + jwtExpiration + 1000); // Allow 1 second tolerance
    }

    // ========== CLAIMS EXTRACTION TESTS ==========

    @Test
    @DisplayName("Should extract all claims from valid token")
    void testGetClaimsFromToken_ValidToken_Success() {
        // Arrange
        String username = "john.doe@company.com";
        Authentication authentication = createMockAuthentication(username, "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Should verify issued at time is before expiration")
    void testGetClaimsFromToken_IssuedAtBeforeExpiration_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertTrue(claims.getIssuedAt().before(claims.getExpiration()));
    }

    // ========== ROLE/AUTHORITY TESTS ==========

    @Test
    @DisplayName("Should generate token for user with multiple roles")
    void testGenerateToken_MultipleRoles_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN", "ROLE_HR");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should generate token for each role type")
    void testGenerateToken_AllRoleTypes_Success() {
        // Test each role
        String[] roles = {"ROLE_ADMIN", "ROLE_HR", "ROLE_SUPERVISOR", "ROLE_WORKER"};
        
        for (String role : roles) {
            // Arrange
            Authentication authentication = createMockAuthentication("user@company.com", role);

            // Act
            String token = jwtTokenProvider.generateToken(authentication);

            // Assert
            assertNotNull(token);
            assertTrue(jwtTokenProvider.validateToken(token));
        }
    }

    // ========== SECURITY EDGE CASES ==========

    @Test
    @DisplayName("Should reject token with tampered payload")
    void testValidateToken_TamperedPayload_ReturnsFalse() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_WORKER");
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Tamper with the payload (change middle part)
        String[] parts = token.split("\.");
        String tamperedToken = parts[0] + "." + "tamperedPayload" + "." + parts[2];

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with tampered signature")
    void testValidateToken_TamperedSignature_ReturnsFalse() {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Tamper with the signature (change last part)
        String[] parts = token.split("\.");
        String tamperedToken = parts[0] + "." + parts[1] + "." + "tamperedSignature";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle very long username")
    void testGenerateToken_VeryLongUsername_Success() {
        // Arrange
        String longUsername = "a".repeat(255) + "@company.com";
        Authentication authentication = createMockAuthentication(longUsername, "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(longUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should handle username with Unicode characters")
    void testGenerateToken_UnicodeUsername_Success() {
        // Arrange
        String unicodeUsername = "ç¨æ·@company.com";
        Authentication authentication = createMockAuthentication(unicodeUsername, "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(unicodeUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should generate consistent tokens for same user at different times")
    void testGenerateToken_SameUserDifferentTimes_DifferentTokens() throws InterruptedException {
        // Arrange
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");

        // Act
        String token1 = jwtTokenProvider.generateToken(authentication);
        Thread.sleep(10); // Small delay to ensure different issued-at time
        String token2 = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotEquals(token1, token2); // Tokens should be different due to different issued-at times
    }

    @Test
    @DisplayName("Should validate token generated with minimum expiration time")
    void testValidateToken_MinimumExpiration_Success() {
        // Arrange
        JwtTokenProvider minExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(minExpirationProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(minExpirationProvider, "jwtExpiration", 1000L); // 1 second
        minExpirationProvider.init();
        
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = minExpirationProvider.generateToken(authentication);

        // Act
        boolean isValid = minExpirationProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate token generated with maximum expiration time")
    void testValidateToken_MaximumExpiration_Success() {
        // Arrange
        JwtTokenProvider maxExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(maxExpirationProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(maxExpirationProvider, "jwtExpiration", 86400000L); // 24 hours
        maxExpirationProvider.init();
        
        Authentication authentication = createMockAuthentication("john.doe@company.com", "ROLE_ADMIN");
        String token = maxExpirationProvider.generateToken(authentication);

        // Act
        boolean isValid = maxExpirationProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    // ========== HELPER METHODS ==========

    private Authentication createMockAuthentication(String username, String... roles) {
        Collection<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(authorities)
                .build();
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getName()).thenReturn(username);
        
        return authentication;
    }
}