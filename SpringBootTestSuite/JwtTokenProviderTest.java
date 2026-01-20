package com.company.warehouse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JwtTokenProvider
 * Covers token generation, validation, and extraction
 */
@DisplayName("JWT Token Provider Tests")
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpiration;

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "SuperSecretJWTKeyForWarehouseEmployeeMgmtSystem2024TestingPurposes";
        jwtExpiration = 86400000L; // 24 hours

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    @DisplayName("Test generate token with valid username and role")
    public void testGenerateTokenWithValidData() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Test generate token with admin role")
    public void testGenerateTokenWithAdminRole() {
        // Arrange
        String username = "admin.user";
        String role = "ADMIN";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        
        // Verify role claim
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    @DisplayName("Test generate token with supervisor role")
    public void testGenerateTokenWithSupervisorRole() {
        // Arrange
        String username = "supervisor.user";
        String role = "SUPERVISOR";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertEquals("SUPERVISOR", claims.get("role"));
    }

    @Test
    @DisplayName("Test generate token with empty username")
    public void testGenerateTokenWithEmptyUsername() {
        // Arrange
        String username = "";
        String role = "WORKER";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("", extractedUsername);
    }

    @Test
    @DisplayName("Test generate token with null username")
    public void testGenerateTokenWithNullUsername() {
        // Arrange
        String username = null;
        String role = "WORKER";

        // Act & Assert
        assertDoesNotThrow(() -> {
            String token = jwtTokenProvider.generateToken(username, role);
            assertNotNull(token);
        });
    }

    @Test
    @DisplayName("Test generate token with special characters in username")
    public void testGenerateTokenWithSpecialCharactersInUsername() {
        // Arrange
        String username = "user@example.com";
        String role = "WORKER";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Test generate token with very long username")
    public void testGenerateTokenWithLongUsername() {
        // Arrange
        String username = "a".repeat(255);
        String role = "WORKER";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    @DisplayName("Test validate token with valid token returns true")
    public void testValidateTokenWithValidToken() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";
        String token = jwtTokenProvider.generateToken(username, role);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Test validate token with invalid token returns false")
    public void testValidateTokenWithInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate token with null token returns false")
    public void testValidateTokenWithNullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate token with empty token returns false")
    public void testValidateTokenWithEmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate token with malformed token returns false")
    public void testValidateTokenWithMalformedToken() {
        // Arrange
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate token with tampered signature returns false")
    public void testValidateTokenWithTamperedSignature() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";
        String token = jwtTokenProvider.generateToken(username, role);
        
        // Tamper with the signature
        String[] parts = token.split("\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tamperedsignature";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate token with expired token returns false")
    public void testValidateTokenWithExpiredToken() throws InterruptedException {
        // Arrange - Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpiration", 1L); // 1 millisecond

        String username = "john.doe";
        String role = "WORKER";
        String token = shortExpirationProvider.generateToken(username, role);

        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpirationProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    // ========== USERNAME EXTRACTION TESTS ==========

    @Test
    @DisplayName("Test get username from valid token")
    public void testGetUsernameFromValidToken() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";
        String token = jwtTokenProvider.generateToken(username, role);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Test get username from token with special characters")
    public void testGetUsernameFromTokenWithSpecialCharacters() {
        // Arrange
        String username = "user@example.com";
        String role = "WORKER";
        String token = jwtTokenProvider.generateToken(username, role);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Test get username from token with numeric username")
    public void testGetUsernameFromTokenWithNumericUsername() {
        // Arrange
        String username = "12345";
        String role = "WORKER";
        String token = jwtTokenProvider.generateToken(username, role);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Test get username from invalid token throws exception")
    public void testGetUsernameFromInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    @DisplayName("Test get username from null token throws exception")
    public void testGetUsernameFromNullToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    // ========== TOKEN EXPIRATION TESTS ==========

    @Test
    @DisplayName("Test token contains correct expiration time")
    public void testTokenContainsCorrectExpiration() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";
        long beforeGeneration = System.currentTimeMillis();

        // Act
        String token = jwtTokenProvider.generateToken(username, role);
        long afterGeneration = System.currentTimeMillis();

        // Assert
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date expiration = claims.getExpiration();
        long expirationTime = expiration.getTime();
        
        // Expiration should be approximately current time + jwtExpiration
        assertTrue(expirationTime >= beforeGeneration + jwtExpiration);
        assertTrue(expirationTime <= afterGeneration + jwtExpiration + 1000); // Allow 1 second tolerance
    }

    @Test
    @DisplayName("Test token contains issued at time")
    public void testTokenContainsIssuedAtTime() {
        // Arrange
        String username = "john.doe";
        String role = "WORKER";
        long beforeGeneration = System.currentTimeMillis();

        // Act
        String token = jwtTokenProvider.generateToken(username, role);
        long afterGeneration = System.currentTimeMillis();

        // Assert
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date issuedAt = claims.getIssuedAt();
        long issuedAtTime = issuedAt.getTime();
        
        assertTrue(issuedAtTime >= beforeGeneration);
        assertTrue(issuedAtTime <= afterGeneration);
    }

    // ========== ROLE CLAIM TESTS ==========

    @Test
    @DisplayName("Test token contains correct role claim")
    public void testTokenContainsCorrectRoleClaim() {
        // Arrange
        String username = "john.doe";
        String role = "SUPERVISOR";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertEquals(role, claims.get("role"));
    }

    @Test
    @DisplayName("Test token with null role")
    public void testTokenWithNullRole() {
        // Arrange
        String username = "john.doe";
        String role = null;

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertNull(claims.get("role"));
    }

    @Test
    @DisplayName("Test token with empty role")
    public void testTokenWithEmptyRole() {
        // Arrange
        String username = "john.doe";
        String role = "";

        // Act
        String token = jwtTokenProvider.generateToken(username, role);

        // Assert
        assertNotNull(token);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertEquals("", claims.get("role"));
    }
}