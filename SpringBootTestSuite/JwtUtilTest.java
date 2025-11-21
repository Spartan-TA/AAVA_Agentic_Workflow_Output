package com.warehouse.ems.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for JwtUtil
 * Tests cover token generation, validation, expiration, and edge cases
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails testUserDetails;
    private final String TEST_USERNAME = "testuser@warehouse.com";
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        // Set secret key for testing
        jwtUtil.setSecretKey("testSecretKeyForJwtTokenGenerationAndValidation123456789");
        jwtUtil.setExpirationMs(3600000L); // 1 hour

        testUserDetails = User.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .authorities(new ArrayList<>())
                .build();
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    public void testGenerateToken_ValidUserDetails_ReturnsToken() {
        // Act
        String token = jwtUtil.generateToken(testUserDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    public void testGenerateToken_NullUserDetails_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.generateToken(null);
        });
    }

    @Test
    public void testGenerateToken_EmptyUsername_ThrowsException() {
        // Arrange
        UserDetails emptyUser = User.builder()
                .username("")
                .password(TEST_PASSWORD)
                .authorities(new ArrayList<>())
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.generateToken(emptyUser);
        });
    }

    @Test
    public void testGenerateToken_MultipleTokens_AreDifferent() {
        // Act
        String token1 = jwtUtil.generateToken(testUserDetails);
        try {
            Thread.sleep(10); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtil.generateToken(testUserDetails);

        // Assert
        assertNotEquals(token1, token2); // Tokens should be different due to timestamp
    }

    @Test
    public void testGenerateToken_ContainsUsername() {
        // Act
        String token = jwtUtil.generateToken(testUserDetails);
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    public void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);

        // Act
        boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null, testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("", testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "invalid.token.format";

        // Act
        boolean isValid = jwtUtil.validateToken(malformedToken, testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_WrongUsername_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);
        UserDetails differentUser = User.builder()
                .username("different@warehouse.com")
                .password(TEST_PASSWORD)
                .authorities(new ArrayList<>())
                .build();

        // Act
        boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange - Create token with very short expiration
        jwtUtil.setExpirationMs(1L); // 1 millisecond
        String token = jwtUtil.generateToken(testUserDetails);

        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_TamperedToken_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // Act
        boolean isValid = jwtUtil.validateToken(tamperedToken, testUserDetails);

        // Assert
        assertFalse(isValid);
    }

    // ========== USERNAME EXTRACTION TESTS ==========

    @Test
    public void testExtractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    public void testExtractUsername_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(null);
        });
    }

    @Test
    public void testExtractUsername_EmptyToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername("");
        });
    }

    @Test
    public void testExtractUsername_MalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "not.a.valid.jwt";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(malformedToken);
        });
    }

    // ========== EXPIRATION TESTS ==========

    @Test
    public void testExtractExpiration_ValidToken_ReturnsExpirationDate() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    public void testIsTokenExpired_ValidToken_ReturnsFalse() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    public void testIsTokenExpired_ExpiredToken_ReturnsTrue() {
        // Arrange - Create token with very short expiration
        jwtUtil.setExpirationMs(1L);
        String token = jwtUtil.generateToken(testUserDetails);

        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    public void testExtractExpiration_ExpiredToken_ThrowsException() {
        // Arrange
        jwtUtil.setExpirationMs(1L);
        String token = jwtUtil.generateToken(testUserDetails);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractExpiration(token);
        });
    }

    // ========== CLAIMS EXTRACTION TESTS ==========

    @Test
    public void testExtractAllClaims_ValidToken_ReturnsClaims() {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);

        // Act
        Claims claims = jwtUtil.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    public void testExtractAllClaims_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractAllClaims(null);
        });
    }

    @Test
    public void testExtractAllClaims_MalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "invalid.token";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractAllClaims(malformedToken);
        });
    }

    // ========== ISSUED AT TESTS ==========

    @Test
    public void testExtractIssuedAt_ValidToken_ReturnsIssuedDate() {
        // Arrange
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(testUserDetails);
        Date afterGeneration = new Date();

        // Act
        Date issuedAt = jwtUtil.extractIssuedAt(token);

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.after(beforeGeneration) || issuedAt.equals(beforeGeneration));
        assertTrue(issuedAt.before(afterGeneration) || issuedAt.equals(afterGeneration));
    }

    // ========== TOKEN REFRESH TESTS ==========

    @Test
    public void testRefreshToken_ValidToken_ReturnsNewToken() {
        // Arrange
        String originalToken = jwtUtil.generateToken(testUserDetails);

        try {
            Thread.sleep(10); // Small delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        String refreshedToken = jwtUtil.refreshToken(originalToken);

        // Assert
        assertNotNull(refreshedToken);
        assertNotEquals(originalToken, refreshedToken);
        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(refreshedToken));
    }

    @Test
    public void testRefreshToken_ExpiredToken_ThrowsException() {
        // Arrange
        jwtUtil.setExpirationMs(1L);
        String token = jwtUtil.generateToken(testUserDetails);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.refreshToken(token);
        });
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testGenerateToken_VeryLongUsername_Success() {
        // Arrange
        String longUsername = "a".repeat(255) + "@warehouse.com";
        UserDetails longUser = User.builder()
                .username(longUsername)
                .password(TEST_PASSWORD)
                .authorities(new ArrayList<>())
                .build();

        // Act
        String token = jwtUtil.generateToken(longUser);

        // Assert
        assertNotNull(token);
        assertEquals(longUsername, jwtUtil.extractUsername(token));
    }

    @Test
    public void testGenerateToken_SpecialCharactersInUsername_Success() {
        // Arrange
        String specialUsername = "user+test@warehouse.com";
        UserDetails specialUser = User.builder()
                .username(specialUsername)
                .password(TEST_PASSWORD)
                .authorities(new ArrayList<>())
                .build();

        // Act
        String token = jwtUtil.generateToken(specialUser);

        // Assert
        assertNotNull(token);
        assertEquals(specialUsername, jwtUtil.extractUsername(token));
    }

    @Test
    public void testValidateToken_ConcurrentValidation_Success() throws InterruptedException {
        // Arrange
        String token = jwtUtil.generateToken(testUserDetails);
        final boolean[] results = new boolean[10];

        // Act - Validate token concurrently
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = jwtUtil.validateToken(token, testUserDetails);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - All validations should succeed
        for (boolean result : results) {
            assertTrue(result);
        }
    }

    @Test
    public void testGenerateToken_WithCustomClaims_Success() {
        // Arrange
        String token = jwtUtil.generateTokenWithClaims(testUserDetails, "role", "ADMIN");

        // Act
        Claims claims = jwtUtil.extractAllClaims(token);

        // Assert
        assertNotNull(token);
        assertEquals("ADMIN", claims.get("role"));
        assertEquals(TEST_USERNAME, claims.getSubject());
    }

    @Test
    public void testValidateToken_BoundaryExpiration_Success() {
        // Arrange - Token expires in 1 second
        jwtUtil.setExpirationMs(1000L);
        String token = jwtUtil.generateToken(testUserDetails);

        // Act - Validate immediately
        boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }
}