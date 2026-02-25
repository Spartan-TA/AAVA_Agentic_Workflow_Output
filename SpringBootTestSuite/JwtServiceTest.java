package com.warehouse.employee.management.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JwtService
 * Tests cover:
 * - Token generation
 * - Token validation
 * - Token expiration
 * - Claims extraction
 * - Edge cases (null, empty, malformed tokens)
 * - Security scenarios
 */
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUserDetails;
    private String testUsername;
    private List<SimpleGrantedAuthority> testAuthorities;

    @BeforeEach
    void setUp() {
        // Initialize JwtService with test secret and expiration
        jwtService = new JwtService();
        jwtService.setSecretKey("testSecretKeyForJWTTokenGenerationAndValidationThatIsLongEnough");
        jwtService.setExpirationTime(3600000L); // 1 hour

        // Setup test user
        testUsername = "testuser@warehouse.com";
        testAuthorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_HR")
        );
        testUserDetails = User.builder()
                .username(testUsername)
                .password("password")
                .authorities(testAuthorities)
                .build();
    }

    // ==================== TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Generate Token - Normal Case - Should Create Valid Token")
    void testGenerateToken_NormalCase_Success() {
        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Generate Token - With Extra Claims - Should Include Claims")
    void testGenerateToken_WithExtraClaims_Success() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("employeeId", "EMP001");
        extraClaims.put("department", "Warehouse");

        // Act
        String token = jwtService.generateToken(extraClaims, testUserDetails);

        // Assert
        assertNotNull(token);
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("EMP001", claims.get("employeeId"));
        assertEquals("Warehouse", claims.get("department"));
    }

    @Test
    @DisplayName("Generate Token - Null UserDetails - Should Throw Exception")
    void testGenerateToken_NullUserDetails_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtService.generateToken(null);
        });
    }

    @Test
    @DisplayName("Generate Token - Empty Username - Should Create Token")
    void testGenerateToken_EmptyUsername_Success() {
        // Arrange
        UserDetails emptyUser = User.builder()
                .username("")
                .password("password")
                .authorities(testAuthorities)
                .build();

        // Act
        String token = jwtService.generateToken(emptyUser);

        // Assert
        assertNotNull(token);
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Validate Token - Valid Token - Should Return True")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUserDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate Token - Wrong User - Should Return False")
    void testValidateToken_WrongUser_ReturnsFalse() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        UserDetails differentUser = User.builder()
                .username("different@warehouse.com")
                .password("password")
                .authorities(testAuthorities)
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Null Token - Should Throw Exception")
    void testValidateToken_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid(null, testUserDetails);
        });
    }

    @Test
    @DisplayName("Validate Token - Empty Token - Should Throw Exception")
    void testValidateToken_EmptyToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid("", testUserDetails);
        });
    }

    @Test
    @DisplayName("Validate Token - Malformed Token - Should Throw Exception")
    void testValidateToken_MalformedToken_ThrowsException() {
        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.isTokenValid("invalid.token.format", testUserDetails);
        });
    }

    @Test
    @DisplayName("Validate Token - Token with Invalid Signature - Should Throw Exception")
    void testValidateToken_InvalidSignature_ThrowsException() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered123";

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtService.isTokenValid(tamperedToken, testUserDetails);
        });
    }

    // ==================== USERNAME EXTRACTION TESTS ====================

    @Test
    @DisplayName("Extract Username - Valid Token - Should Return Username")
    void testExtractUsername_ValidToken_Success() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("Extract Username - Null Token - Should Throw Exception")
    void testExtractUsername_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(null);
        });
    }

    @Test
    @DisplayName("Extract Username - Malformed Token - Should Throw Exception")
    void testExtractUsername_MalformedToken_ThrowsException() {
        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUsername("malformed.token");
        });
    }

    // ==================== EXPIRATION TESTS ====================

    @Test
    @DisplayName("Extract Expiration - Valid Token - Should Return Future Date")
    void testExtractExpiration_ValidToken_ReturnsFutureDate() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Is Token Expired - Fresh Token - Should Return False")
    void testIsTokenExpired_FreshToken_ReturnsFalse() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Is Token Expired - Expired Token - Should Return True")
    void testIsTokenExpired_ExpiredToken_ReturnsTrue() {
        // Arrange
        jwtService.setExpirationTime(-1000L); // Set to past
        String token = jwtService.generateToken(testUserDetails);
        jwtService.setExpirationTime(3600000L); // Reset

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Validate Token - Expired Token - Should Throw Exception")
    void testValidateToken_ExpiredToken_ThrowsException() {
        // Arrange
        jwtService.setExpirationTime(-1000L); // Set to past
        String token = jwtService.generateToken(testUserDetails);
        jwtService.setExpirationTime(3600000L); // Reset

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(token, testUserDetails);
        });
    }

    // ==================== CLAIMS EXTRACTION TESTS ====================

    @Test
    @DisplayName("Extract All Claims - Valid Token - Should Return Claims")
    void testExtractAllClaims_ValidToken_Success() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("employeeId", "EMP001");
        String token = jwtService.generateToken(extraClaims, testUserDetails);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("EMP001", claims.get("employeeId"));
    }

    @Test
    @DisplayName("Extract Claim - Custom Claim - Should Return Claim Value")
    void testExtractClaim_CustomClaim_Success() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("department", "Warehouse");
        String token = jwtService.generateToken(extraClaims, testUserDetails);

        // Act
        String department = jwtService.extractClaim(token, claims -> claims.get("department", String.class));

        // Assert
        assertEquals("Warehouse", department);
    }

    @Test
    @DisplayName("Extract Claim - Non-Existent Claim - Should Return Null")
    void testExtractClaim_NonExistentClaim_ReturnsNull() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        String nonExistent = jwtService.extractClaim(token, claims -> claims.get("nonExistent", String.class));

        // Assert
        assertNull(nonExistent);
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Generate Token - Very Long Username - Should Create Token")
    void testGenerateToken_VeryLongUsername_Success() {
        // Arrange
        String longUsername = "a".repeat(1000) + "@warehouse.com";
        UserDetails longUser = User.builder()
                .username(longUsername)
                .password("password")
                .authorities(testAuthorities)
                .build();

        // Act
        String token = jwtService.generateToken(longUser);

        // Assert
        assertNotNull(token);
        assertEquals(longUsername, jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Generate Token - Special Characters in Username - Should Create Token")
    void testGenerateToken_SpecialCharacters_Success() {
        // Arrange
        String specialUsername = "user+test@warehouse.com";
        UserDetails specialUser = User.builder()
                .username(specialUsername)
                .password("password")
                .authorities(testAuthorities)
                .build();

        // Act
        String token = jwtService.generateToken(specialUser);

        // Assert
        assertNotNull(token);
        assertEquals(specialUsername, jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Generate Token - Maximum Expiration Time - Should Create Token")
    void testGenerateToken_MaxExpiration_Success() {
        // Arrange
        jwtService.setExpirationTime(Long.MAX_VALUE / 2); // Very long expiration

        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Assert
        assertNotNull(token);
        assertFalse(jwtService.isTokenExpired(token));
        
        // Reset
        jwtService.setExpirationTime(3600000L);
    }

    @Test
    @DisplayName("Generate Token - Minimum Expiration Time - Should Create Token")
    void testGenerateToken_MinExpiration_Success() {
        // Arrange
        jwtService.setExpirationTime(1L); // 1 millisecond

        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Assert
        assertNotNull(token);
        
        // Reset
        jwtService.setExpirationTime(3600000L);
    }

    @Test
    @DisplayName("Generate Token - Many Extra Claims - Should Include All Claims")
    void testGenerateToken_ManyExtraClaims_Success() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            extraClaims.put("claim" + i, "value" + i);
        }

        // Act
        String token = jwtService.generateToken(extraClaims, testUserDetails);

        // Assert
        assertNotNull(token);
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("value0", claims.get("claim0"));
        assertEquals("value99", claims.get("claim99"));
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @DisplayName("Token Tampering - Modified Payload - Should Fail Validation")
    void testTokenTampering_ModifiedPayload_FailsValidation() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        String[] parts = token.split("\.");
        // Modify the payload (middle part)
        String tamperedToken = parts[0] + "." + Base64.getEncoder().encodeToString("tampered".getBytes()) + "." + parts[2];

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid(tamperedToken, testUserDetails);
        });
    }

    @Test
    @DisplayName("Token Reuse - Same Token Multiple Times - Should Remain Valid")
    void testTokenReuse_SameTokenMultipleTimes_RemainsValid() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act & Assert
        assertTrue(jwtService.isTokenValid(token, testUserDetails));
        assertTrue(jwtService.isTokenValid(token, testUserDetails));
        assertTrue(jwtService.isTokenValid(token, testUserDetails));
    }

    @Test
    @DisplayName("Different Tokens - Same User - Should Both Be Valid")
    void testDifferentTokens_SameUser_BothValid() {
        // Arrange
        String token1 = jwtService.generateToken(testUserDetails);
        String token2 = jwtService.generateToken(testUserDetails);

        // Act & Assert
        assertTrue(jwtService.isTokenValid(token1, testUserDetails));
        assertTrue(jwtService.isTokenValid(token2, testUserDetails));
        assertNotEquals(token1, token2); // Tokens should be different
    }

    @Test
    @DisplayName("Extract Username - Token from Different Secret - Should Throw Exception")
    void testExtractUsername_DifferentSecret_ThrowsException() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);
        jwtService.setSecretKey("differentSecretKeyForJWTTokenGenerationAndValidation");

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtService.extractUsername(token);
        });
        
        // Reset
        jwtService.setSecretKey("testSecretKeyForJWTTokenGenerationAndValidationThatIsLongEnough");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Generate Token - User with No Authorities - Should Create Token")
    void testGenerateToken_NoAuthorities_Success() {
        // Arrange
        UserDetails noAuthUser = User.builder()
                .username(testUsername)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        String token = jwtService.generateToken(noAuthUser);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, noAuthUser));
    }

    @Test
    @DisplayName("Generate Token - Null Extra Claims - Should Create Token")
    void testGenerateToken_NullExtraClaims_Success() {
        // Act
        String token = jwtService.generateToken(null, testUserDetails);

        // Assert
        assertNotNull(token);
    }

    @Test
    @DisplayName("Generate Token - Empty Extra Claims - Should Create Token")
    void testGenerateToken_EmptyExtraClaims_Success() {
        // Act
        String token = jwtService.generateToken(new HashMap<>(), testUserDetails);

        // Assert
        assertNotNull(token);
    }

    @Test
    @DisplayName("Token with Whitespace - Should Throw Exception")
    void testTokenWithWhitespace_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid("token with spaces", testUserDetails);
        });
    }

    @Test
    @DisplayName("Token with Only Dots - Should Throw Exception")
    void testTokenWithOnlyDots_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid("...", testUserDetails);
        });
    }
}