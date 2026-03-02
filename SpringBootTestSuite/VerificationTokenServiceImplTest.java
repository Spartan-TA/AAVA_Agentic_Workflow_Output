package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.entity.VerificationToken;
import com.example.usermanagement.exception.TokenExpiredException;
import com.example.usermanagement.exception.TokenNotFoundException;
import com.example.usermanagement.repository.VerificationTokenRepository;
import com.example.usermanagement.service.impl.VerificationTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for VerificationTokenServiceImpl covering token creation, validation, expiry, and edge cases.
 */
public class VerificationTokenServiceImplTest {
    @Mock private VerificationTokenRepository tokenRepository;
    @InjectMocks private VerificationTokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateToken_Success() {
        User user = new User();
        VerificationToken token = new VerificationToken();
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(token);
        VerificationToken result = tokenService.createToken(user);
        assertNotNull(result);
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void testValidateToken_ValidToken_Success() {
        VerificationToken token = new VerificationToken();
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        token.setUser(new User());
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        User user = tokenService.validateToken("validToken");
        assertNotNull(user);
    }

    @Test
    void testValidateToken_TokenNotFound_ThrowsException() {
        when(tokenRepository.findByToken("notfound")).thenReturn(Optional.empty());
        assertThrows(TokenNotFoundException.class, () -> tokenService.validateToken("notfound"));
    }

    @Test
    void testValidateToken_TokenExpired_ThrowsException() {
        VerificationToken token = new VerificationToken();
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));
        assertThrows(TokenExpiredException.class, () -> tokenService.validateToken("expired"));
    }

    @Test
    void testCreateToken_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenService.createToken(null));
    }

    @Test
    void testValidateToken_NullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenService.validateToken(null));
    }

    @Test
    void testValidateToken_EmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenService.validateToken(""));
    }
}
