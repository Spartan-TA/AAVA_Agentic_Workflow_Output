package com.wms.ems.integration;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String jwtSecret;
    private long jwtExpirationMs;

    @BeforeEach
    void setUp() {
        // Use a valid 32+ byte secret for HS256
        jwtSecret = "0123456789abcdef0123456789abcdef0123456789abcdef";
        jwtExpirationMs = 1000 * 60 * 60; // 1 hour
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
    }

    // Test generateToken normal case
    @Test
    void testGenerateToken_ValidInput_ReturnsToken() {
        String token = jwtTokenProvider.generateToken("user1", "ADMIN");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    // Test validateToken valid token
    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken("user2", "HR");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    // Test validateToken expired token
    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L); // expired
        String token = jwtTokenProvider.generateToken("user3", "WORKER");
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    // Test validateToken malformed token
    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        String malformed = "not.a.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(malformed));
    }

    // Test getUsernameFromToken normal case
    @Test
    void testGetUsernameFromToken_ValidToken_ReturnsUsername() {
        String token = jwtTokenProvider.generateToken("user4", "SUPERVISOR");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("user4", username);
    }

    // Test getRoleFromToken normal case
    @Test
    void testGetRoleFromToken_ValidToken_ReturnsRole() {
        String token = jwtTokenProvider.generateToken("user5", "HR");
        String role = jwtTokenProvider.getRoleFromToken(token);
        assertEquals("HR", role);
    }

    // Test getUsernameFromToken malformed token
    @Test
    void testGetUsernameFromToken_MalformedToken_ThrowsException() {
        String malformed = "abc.def.ghi";
        assertThrows(JwtException.class, () -> jwtTokenProvider.getUsernameFromToken(malformed));
    }

    // Test getRoleFromToken malformed token
    @Test
    void testGetRoleFromToken_MalformedToken_ThrowsException() {
        String malformed = "abc.def.ghi";
        assertThrows(JwtException.class, () -> jwtTokenProvider.getRoleFromToken(malformed));
    }

    // Test generateToken with empty username
    @Test
    void testGenerateToken_EmptyUsername_ReturnsToken() {
        String token = jwtTokenProvider.generateToken("", "ADMIN");
        assertNotNull(token);
    }

    // Test generateToken with null role
    @Test
    void testGenerateToken_NullRole_ReturnsToken() {
        String token = jwtTokenProvider.generateToken("user6", null);
        assertNotNull(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        assertNull(role);
    }
}
