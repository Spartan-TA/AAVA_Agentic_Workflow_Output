package com.company.wems.security.jwt;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetails userDetails;

    private String secret = "mysecretkeymysecretkeymysecretkeymysecretkey";
    private long expiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenProvider = new JwtTokenProvider();
        // Set fields via reflection since @Value is not used in test
        TestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        TestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expiration);
        jwtTokenProvider.init();
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Should generate token with valid UserDetails")
    void testGenerateToken_ValidUserDetails_Success() {
        String token = jwtTokenProvider.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("Should validate token with valid token")
    void testValidateToken_ValidToken_True() {
        String token = jwtTokenProvider.generateToken(userDetails);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should not validate token with expired token")
    void testValidateToken_ExpiredToken_False() {
        // Create a token with past expiration
        TestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        jwtTokenProvider.init();
        String expiredToken = jwtTokenProvider.generateToken(userDetails);
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
        // Reset expiration
        TestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expiration);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Should not validate token with invalid token")
    void testValidateToken_InvalidToken_False() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should extract username from token")
    void testGetUsernameFromToken_ValidToken_Success() {
        String token = jwtTokenProvider.generateToken(userDetails);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void testGetExpirationDateFromToken_ValidToken_Success() {
        String token = jwtTokenProvider.generateToken(userDetails);
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should throw exception for malformed token (edge case)")
    void testGetClaims_MalformedToken_ThrowsException() {
        String malformed = "malformed.token";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.getUsernameFromToken(malformed));
        assertTrue(ex.getMessage().contains("Invalid JWT token"));
    }

    // Utility for reflection field set
    static class TestUtils {
        static void setField(Object target, String fieldName, Object value) {
            try {
                java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
