package com.wms.ems.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Comprehensive JUnit test suite for JwtTokenProvider
 * Covers: Token generation, validation, expiration, claims extraction
 * Epic: E03 - Role Based Access Control (RBAC)
 */
@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret = "testSecretKeyThatIsLongEnoughForHS512Algorithm";
    private long jwtExpirationMs = 3600000; // 1 hour

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
        jwtTokenProvider.init();
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    public void testGenerateToken_ValidAuthentication_ReturnsToken() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
    }

    @Test
    public void testGenerateToken_NullAuthentication_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateToken(null);
        });
    }

    @Test
    public void testGenerateToken_MultipleRoles_IncludesAllRoles() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN", "ROLE_HR");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    public void testValidateToken_ValidToken_ReturnsTrue() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_MalformedToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.format");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Arrange
        String tokenWithInvalidSignature = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYwOTQ1OTIwMCwiZXhwIjoxNjA5NDYyODAwfQ.invalid_signature";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithInvalidSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpirationMs", 1L); // 1ms expiration
        expiredTokenProvider.init();

        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = expiredTokenProvider.generateToken(authentication);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = expiredTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    // ========== USERNAME EXTRACTION TESTS ==========

    @Test
    public void testGetUsernameFromToken_ValidToken_ReturnsUsername() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    public void testGetUsernameFromToken_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    @Test
    public void testGetUsernameFromToken_EmptyToken_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getUsernameFromToken("");
        });
    }

    @Test
    public void testGetUsernameFromToken_MalformedToken_ThrowsException() {
        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtTokenProvider.getUsernameFromToken("invalid.token");
        });
    }

    // ========== ROLES EXTRACTION TESTS ==========

    @Test
    public void testGetRolesFromToken_ValidToken_ReturnsRoles() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN", "ROLE_HR");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Collection<? extends GrantedAuthority> roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_HR")));
    }

    @Test
    public void testGetRolesFromToken_SingleRole_ReturnsRole() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_WORKER");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Collection<? extends GrantedAuthority> roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_WORKER", roles.iterator().next().getAuthority());
    }

    // ========== EXPIRATION TESTS ==========

    @Test
    public void testGetExpirationDateFromToken_ValidToken_ReturnsDate() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    public void testIsTokenExpired_ValidToken_ReturnsFalse() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    public void testIsTokenExpired_ExpiredToken_ReturnsTrue() {
        // Arrange
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpirationMs", 1L);
        expiredTokenProvider.init();

        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = expiredTokenProvider.generateToken(authentication);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = expiredTokenProvider.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    // ========== CLAIMS EXTRACTION TESTS ==========

    @Test
    public void testGetAllClaimsFromToken_ValidToken_ReturnsClaims() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testGenerateToken_UsernameWithSpecialCharacters_Success() {
        // Arrange
        Authentication authentication = createMockAuthentication("test.user@example.com", "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("test.user@example.com", username);
    }

    @Test
    public void testGenerateToken_LongUsername_Success() {
        // Arrange
        String longUsername = "a".repeat(255);
        Authentication authentication = createMockAuthentication(longUsername, "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(longUsername, username);
    }

    @Test
    public void testGenerateToken_NoRoles_ThrowsException() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateToken(authentication);
        });
    }

    @Test
    public void testValidateToken_TokenWithWhitespace_ReturnsFalse() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);
        String tokenWithWhitespace = " " + token + " ";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWhitespace);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testGenerateToken_SameUserMultipleTimes_GeneratesDifferentTokens() {
        // Arrange
        Authentication authentication = createMockAuthentication("testuser", "ROLE_ADMIN");

        // Act
        String token1 = jwtTokenProvider.generateToken(authentication);
        try {
            Thread.sleep(1000); // Wait 1 second to ensure different issued-at time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotEquals(token1, token2);
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
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        return authentication;
    }
}