package com.warehouse.management.security;

import com.warehouse.management.employee.EmployeeRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for JwtTokenProvider
 * Tests cover token generation, validation, expiration, and security edge cases
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    private String testUsername;
    private UUID testUserId;
    private String secretKey;
    private long validityInMilliseconds;

    @BeforeEach
    void setUp() {
        testUsername = "john.doe@warehouse.com";
        testUserId = UUID.randomUUID();
        secretKey = "testSecretKeyForJwtTokenGenerationAndValidationMustBeLongEnough";
        validityInMilliseconds = 3600000; // 1 hour
        
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", validityInMilliseconds);
        
        // Initialize the provider
        jwtTokenProvider.init();
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    void testGenerateToken_ValidAuthentication_Success() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_MultipleRoles_Success() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_HR")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        String roles = jwtTokenProvider.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_HR"));
    }

    @Test
    void testGenerateToken_NullAuthentication_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtTokenProvider.generateToken(null, testUserId);
        });
    }

    @Test
    void testGenerateToken_NullUserId_ThrowsException() {
        // Arrange
        when(authentication.getName()).thenReturn(testUsername);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateToken(authentication, null);
        });
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", 1L); // 1 millisecond
        jwtTokenProvider.init();
        
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtTokenProvider.validateToken(token);
        });
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtTokenProvider.validateToken(malformedToken);
        });
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.validateToken("");
        });
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.validateToken(null);
        });
    }

    @Test
    void testValidateToken_TamperedToken_ReturnsFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);
        
        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtTokenProvider.validateToken(tamperedToken);
        });
    }

    // ========== USERNAME EXTRACTION TESTS ==========

    @Test
    void testGetUsernameFromToken_ValidToken_ReturnsUsername() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void testGetUsernameFromToken_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    // ========== USER ID EXTRACTION TESTS ==========

    @Test
    void testGetUserIdFromToken_ValidToken_ReturnsUserId() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testGetUserIdFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtTokenProvider.getUserIdFromToken(invalidToken);
        });
    }

    // ========== ROLES EXTRACTION TESTS ==========

    @Test
    void testGetRolesFromToken_ValidToken_ReturnsRoles() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_HR")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        String roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_HR"));
    }

    @Test
    void testGetRolesFromToken_SingleRole_ReturnsRole() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        String roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertEquals("ROLE_WORKER", roles);
    }

    // ========== TOKEN EXPIRATION TESTS ==========

    @Test
    void testGetExpirationDateFromToken_ValidToken_ReturnsDate() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testIsTokenExpired_ValidToken_ReturnsFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", 1L);
        jwtTokenProvider.init();
        
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testGenerateToken_SpecialCharactersInUsername_Success() {
        // Arrange
        String specialUsername = "user+test@warehouse.com";
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(specialUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        assertEquals(specialUsername, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void testGenerateToken_VeryLongUsername_Success() {
        // Arrange
        String longUsername = "a".repeat(255) + "@warehouse.com";
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(longUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        assertEquals(longUsername, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void testValidateToken_TokenWithWhitespace_ThrowsException() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);
        String tokenWithWhitespace = "  " + token + "  ";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWhitespace.trim());

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testGenerateToken_NoRoles_Success() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList();
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        String roles = jwtTokenProvider.getRolesFromToken(token);
        assertTrue(roles == null || roles.isEmpty());
    }

    @Test
    void testValidateToken_TokenFromDifferentSecret_ThrowsException() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);
        
        // Change the secret key
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "differentSecretKeyForTestingPurposesMustBeLongEnough");
        jwtTokenProvider.init();

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtTokenProvider.validateToken(token);
        });
    }

    @Test
    void testGenerateToken_AllEmployeeRoles_Success() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_HR"),
                new SimpleGrantedAuthority("ROLE_SUPERVISOR"),
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Assert
        assertNotNull(token);
        String roles = jwtTokenProvider.getRolesFromToken(token);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_HR"));
        assertTrue(roles.contains("ROLE_SUPERVISOR"));
        assertTrue(roles.contains("ROLE_WORKER"));
    }

    @Test
    void testGetAllClaimsFromToken_ValidToken_ReturnsAllClaims() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        
        when(authentication.getName()).thenReturn(testUsername);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        
        String token = jwtTokenProvider.generateToken(authentication, testUserId);

        // Act
        Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
        assertNotNull(claims.get("userId"));
        assertNotNull(claims.get("roles"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}