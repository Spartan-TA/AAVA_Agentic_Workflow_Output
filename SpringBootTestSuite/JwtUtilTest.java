package com.warehouse.employeemgmt.common.util;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil covering normal, boundary, edge, and exception scenarios.
 */
class JwtUtilTest {
    private JwtUtil jwtUtil;
    private final String secret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"; // 64 chars
    private final long expiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", expiration);
    }

    @Test
    @DisplayName("generateToken creates a valid JWT and validateToken parses it")
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken("user1", "ADMIN");
        Jws<Claims> claims = jwtUtil.validateToken(token);
        assertEquals("user1", claims.getBody().getSubject());
        assertEquals("ADMIN", claims.getBody().get("role"));
    }

    @Test
    @DisplayName("extractUsername returns correct username from token")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("user2", "HR");
        String username = jwtUtil.extractUsername(token);
        assertEquals("user2", username);
    }

    @Test
    @DisplayName("isTokenExpired returns false for fresh token")
    void isTokenExpired_returnsFalseForFreshToken() {
        String token = jwtUtil.generateToken("user3", "WORKER");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenExpired returns true for expired token")
    void isTokenExpired_returnsTrueForExpiredToken() {
        // Create a token with expiration in the past
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L);
        String token = jwtUtil.generateToken("user4", "SUPERVISOR");
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("validateToken throws exception for invalid signature")
    void validateToken_invalidSignature_throwsException() {
        String token = jwtUtil.generateToken("user5", "ADMIN");
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "jwtSecret", "differentsecret0123456789abcdef0123456789abcdef0123456789abcdef0123");
        ReflectionTestUtils.setField(otherUtil, "jwtExpiration", expiration);
        assertThrows(JwtException.class, () -> otherUtil.validateToken(token));
    }

    @Test
    @DisplayName("validateToken throws exception for malformed token")
    void validateToken_malformedToken_throwsException() {
        String malformed = "not.a.jwt.token";
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(malformed));
    }

    @Test
    @DisplayName("isTokenExpired returns true for malformed token")
    void isTokenExpired_malformedToken_returnsTrue() {
        assertTrue(jwtUtil.isTokenExpired("not.a.jwt.token"));
    }
}
