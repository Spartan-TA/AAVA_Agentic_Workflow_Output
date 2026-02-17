package com.example.warehouse.security;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private String jwtSecret = "testSecretKey";
    private long jwtExpiration = 1000; // 1 second for testing

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Use reflection to set private fields
        try {
            var secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtTokenProvider, jwtSecret);

            var expField = JwtTokenProvider.class.getDeclaredField("jwtExpiration");
            expField.setAccessible(true);
            expField.set(jwtTokenProvider, jwtExpiration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateToken_ValidAuthentication_ReturnsToken() {
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateToken_NullAuthentication_ThrowsException() {
        assertThrows(Exception.class, () -> jwtTokenProvider.generateToken(null));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.value"));
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);
        Thread.sleep(1100); // Wait for token to expire
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("malformed.token"));
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void testGetUsernameFromToken_ValidToken_ReturnsUsername() {
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("user", username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtTokenProvider.getUsernameFromToken("invalid.token"));
    }

    @Test
    void testTokenExpiration_AfterExpiryTime_IsInvalid() throws InterruptedException {
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);
        Thread.sleep(1100); // Wait for token to expire
        assertFalse(jwtTokenProvider.validateToken(token));
    }
}