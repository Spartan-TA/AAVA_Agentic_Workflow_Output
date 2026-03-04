package com.warehouse.ems.security;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {
    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private String jwtSecret = "testSecretKey1234567890";
    private long jwtExpirationMs = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    void testGenerateToken_ReturnsValidToken() {
        String token = jwtTokenUtil.generateToken("alice");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetUsernameFromToken_ReturnsCorrectUsername() {
        String token = jwtTokenUtil.generateToken("bob");
        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("bob", username);
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenUtil.generateToken("charlie");
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        Date now = new Date();
        Date expired = new Date(now.getTime() - 1000 * 60 * 60); // 1 hour ago
        String expiredToken = Jwts.builder()
                .setSubject("expired")
                .setIssuedAt(now)
                .setExpiration(expired)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        assertFalse(jwtTokenUtil.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        String malformed = "not.a.jwt.token";
        assertFalse(jwtTokenUtil.validateToken(malformed));
    }

    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        String token = jwtTokenUtil.generateToken("dave");
        String tampered = token.substring(0, token.length() - 1) + "a";
        assertFalse(jwtTokenUtil.validateToken(tampered));
    }

    @Test
    void testValidateToken_UnsupportedToken_ReturnsFalse() {
        String unsupported = Jwts.builder()
                .setSubject("unsupported")
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
        assertFalse(jwtTokenUtil.validateToken(unsupported));
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtTokenUtil.validateToken(""));
    }

    @Test
    void testGetUsernameFromToken_MalformedToken_ThrowsException() {
        String malformed = "not.a.jwt.token";
        assertThrows(Exception.class, () -> jwtTokenUtil.getUsernameFromToken(malformed));
    }

    @Test
    void testGenerateToken_BoundaryUsernameLength() {
        String username = "u".repeat(128);
        String token = jwtTokenUtil.generateToken(username);
        String parsed = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(username, parsed);
    }

    @Test
    void testGenerateToken_XssAndSqlInjection() {
        String username = "<script>alert('xss')</script>";
        String token = jwtTokenUtil.generateToken(username);
        String parsed = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(username, parsed);
        String sql = "'; DROP TABLE users; --";
        String token2 = jwtTokenUtil.generateToken(sql);
        String parsed2 = jwtTokenUtil.getUsernameFromToken(token2);
        assertEquals(sql, parsed2);
    }

    @Test
    void testGenerateToken_NullUsername_ThrowsException() {
        assertThrows(NullPointerException.class, () -> jwtTokenUtil.generateToken(null));
    }
}
