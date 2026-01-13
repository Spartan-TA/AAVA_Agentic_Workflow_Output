package com.company.wms.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtTokenProvider
 * 
 * Tests cover:
 * - Token generation with valid authentication
 * - Token validation (valid, invalid, expired, malformed)
 * - Username and user ID extraction
 * - Refresh token generation
 * - Null and edge case handling
 * - Security exception scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    private UserPrincipal userPrincipal;
    private String validToken;
    private final String jwtSecret = "mySecretKeyForJWTTokenGenerationAndValidation123456789";
    private final long jwtExpirationMs = 86400000L; // 24 hours
    private final long jwtRefreshExpirationMs = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        // Set up JWT configuration using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", jwtRefreshExpirationMs);

        // Create test user principal
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        userPrincipal = new UserPrincipal(1L, "EMP001", "password", authorities, true);

        // Mock authentication
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
    }

    // ==================== TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Should generate valid JWT token with authentication")
    void testGenerateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\.").length == 3); // JWT has 3 parts
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    @DisplayName("Should generate token with correct username")
    void testGenerateToken_CorrectUsername() {
        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("EMP001", username);
    }

    @Test
    @DisplayName("Should generate token with correct user ID")
    void testGenerateToken_CorrectUserId() {
        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        UserPrincipal anotherUser = new UserPrincipal(2L, "EMP002", "password", authorities, true);
        Authentication anotherAuth = mock(Authentication.class);
        when(anotherAuth.getPrincipal()).thenReturn(anotherUser);

        // Act
        String token1 = jwtTokenProvider.generateToken(authentication);
        String token2 = jwtTokenProvider.generateToken(anotherAuth);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should throw exception when authentication is null")
    void testGenerateToken_NullAuthentication() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtTokenProvider.generateToken(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when principal is null")
    void testGenerateToken_NullPrincipal() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtTokenProvider.generateToken(authentication);
        });
    }

    @Test
    @DisplayName("Should generate token with multiple roles")
    void testGenerateToken_MultipleRoles() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER"),
                new SimpleGrantedAuthority("ROLE_SUPERVISOR")
        );
        UserPrincipal multiRoleUser = new UserPrincipal(1L, "EMP001", "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(multiRoleUser);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    // ==================== REFRESH TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken_Success() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.split("\.").length == 3);
    }

    @Test
    @DisplayName("Should generate different refresh token from access token")
    void testGenerateRefreshToken_DifferentFromAccessToken() {
        // Act
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Assert
        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    @DisplayName("Should throw exception when generating refresh token with null authentication")
    void testGenerateRefreshToken_NullAuthentication() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtTokenProvider.generateRefreshToken(null);
        });
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate correct token")
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateToken_MalformedToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.format");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testValidateToken_InvalidSignature() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered123";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with only header")
    void testValidateToken_OnlyHeader() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("eyJhbGciOiJIUzUxMiJ9");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with only header and payload")
    void testValidateToken_NoSignature() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with whitespace")
    void testValidateToken_WhitespaceToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("   ");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with special characters only")
    void testValidateToken_SpecialCharacters() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("!@#$%^&*()");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject expired token")
    void testValidateToken_ExpiredToken() {
        // Arrange - Set very short expiration
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1L);
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
        
        // Reset expiration
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
    }

    // ==================== USERNAME EXTRACTION TESTS ====================

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("EMP001", username);
    }

    @Test
    @DisplayName("Should throw exception when extracting username from null token")
    void testGetUsernameFromToken_NullToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when extracting username from empty token")
    void testGetUsernameFromToken_EmptyToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken("");
        });
    }

    @Test
    @DisplayName("Should throw exception when extracting username from malformed token")
    void testGetUsernameFromToken_MalformedToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken("invalid.token");
        });
    }

    @Test
    @DisplayName("Should extract username with special characters")
    void testGetUsernameFromToken_SpecialCharacters() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal specialUser = new UserPrincipal(1L, "user@domain.com", "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(specialUser);
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("user@domain.com", username);
    }

    // ==================== USER ID EXTRACTION TESTS ====================

    @Test
    @DisplayName("Should extract user ID from valid token")
    void testGetUserIdFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("Should throw exception when extracting user ID from null token")
    void testGetUserIdFromToken_NullToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when extracting user ID from empty token")
    void testGetUserIdFromToken_EmptyToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken("");
        });
    }

    @Test
    @DisplayName("Should throw exception when extracting user ID from malformed token")
    void testGetUserIdFromToken_MalformedToken() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromToken("invalid.token");
        });
    }

    @Test
    @DisplayName("Should extract large user ID")
    void testGetUserIdFromToken_LargeUserId() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal largeIdUser = new UserPrincipal(999999999L, "EMP999", "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(largeIdUser);
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(999999999L, userId);
    }

    @Test
    @DisplayName("Should extract minimum user ID")
    void testGetUserIdFromToken_MinimumUserId() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal minIdUser = new UserPrincipal(1L, "EMP001", "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(minIdUser);
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(1L, userId);
    }

    // ==================== TOKEN CONSISTENCY TESTS ====================

    @Test
    @DisplayName("Should generate consistent token for same user")
    void testTokenConsistency_SameUser() {
        // Act
        String token1 = jwtTokenProvider.generateToken(authentication);
        String username1 = jwtTokenProvider.getUsernameFromToken(token1);
        Long userId1 = jwtTokenProvider.getUserIdFromToken(token1);

        String token2 = jwtTokenProvider.generateToken(authentication);
        String username2 = jwtTokenProvider.getUsernameFromToken(token2);
        Long userId2 = jwtTokenProvider.getUserIdFromToken(token2);

        // Assert
        assertEquals(username1, username2);
        assertEquals(userId1, userId2);
    }

    @Test
    @DisplayName("Should validate token after extraction")
    void testTokenValidation_AfterExtraction() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertNotNull(username);
        assertNotNull(userId);
        assertTrue(isValid);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle token with very long username")
    void testToken_VeryLongUsername() {
        // Arrange
        String longUsername = "a".repeat(255);
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal longUsernameUser = new UserPrincipal(1L, longUsername, "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(longUsernameUser);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(longUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should handle token with username containing spaces")
    void testToken_UsernameWithSpaces() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal spaceUser = new UserPrincipal(1L, "user name", "password", authorities, true);
        when(authentication.getPrincipal()).thenReturn(spaceUser);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("user name", extractedUsername);
    }

    @Test
    @DisplayName("Should handle token with no authorities")
    void testToken_NoAuthorities() {
        // Arrange
        UserPrincipal noAuthUser = new UserPrincipal(1L, "EMP001", "password", Arrays.asList(), true);
        when(authentication.getPrincipal()).thenReturn(noAuthUser);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should handle inactive user token generation")
    void testToken_InactiveUser() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_WORKER")
        );
        UserPrincipal inactiveUser = new UserPrincipal(1L, "EMP001", "password", authorities, false);
        when(authentication.getPrincipal()).thenReturn(inactiveUser);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }
}