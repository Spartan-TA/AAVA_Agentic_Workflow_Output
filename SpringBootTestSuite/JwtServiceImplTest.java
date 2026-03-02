package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.TokenBlacklistRepository;
import com.example.usermanagement.service.impl.JwtServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtServiceImpl covering JWT generation, validation, blacklisting, and edge cases.
 */
public class JwtServiceImplTest {
    @Mock private TokenBlacklistRepository blacklistRepository;
    @InjectMocks private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateToken_ValidUser_Success() {
        User user = new User();
        user.setEmail("jwtuser@example.com");
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testValidateToken_ValidToken_Success() {
        User user = new User();
        user.setEmail("jwtuser@example.com");
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_BlacklistedToken_False() {
        String token = "blacklisted.jwt.token";
        when(blacklistRepository.existsByToken(token)).thenReturn(true);
        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken_ThrowsException() {
        String token = "invalid.jwt.token";
        assertThrows(JwtException.class, () -> jwtService.validateToken(token));
    }

    @Test
    void testBlacklistToken_Success() {
        String token = "toblacklist.jwt.token";
        jwtService.blacklistToken(token);
        verify(blacklistRepository, times(1)).save(any());
    }

    @Test
    void testBlacklistToken_NullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.blacklistToken(null));
    }

    @Test
    void testBlacklistToken_EmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.blacklistToken(""));
    }
}
