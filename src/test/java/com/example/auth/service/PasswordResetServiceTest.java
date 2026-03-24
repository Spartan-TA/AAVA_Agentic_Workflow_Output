package com.example.auth.service;

import com.example.auth.entity.PasswordResetToken;
import com.example.auth.entity.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.exception.PasswordResetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private PasswordResetService passwordResetService;
    private AutoCloseable closeable;
    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testToken = new PasswordResetToken();
        testToken.setToken(UUID.randomUUID().toString());
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testCreatePasswordResetToken_ValidUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
        doNothing().when(emailService).sendPasswordResetEmail(eq(testUser), anyString());
        assertDoesNotThrow(() -> passwordResetService.createPasswordResetToken("test@example.com"));
        verify(emailService, times(1)).sendPasswordResetEmail(eq(testUser), anyString());
    }

    @Test
    void testCreatePasswordResetToken_NonExistingUser_ThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(PasswordResetException.class, () -> passwordResetService.createPasswordResetToken("notfound@example.com"));
    }

    @Test
    void testCreatePasswordResetToken_NullEmail_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.createPasswordResetToken(null));
    }

    @Test
    void testCreatePasswordResetToken_EmptyEmail_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.createPasswordResetToken(""));
    }

    @Test
    void testValidatePasswordResetToken_ValidToken_Success() {
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        assertDoesNotThrow(() -> passwordResetService.validatePasswordResetToken(testToken.getToken()));
    }

    @Test
    void testValidatePasswordResetToken_ExpiredToken_ThrowsException() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        assertThrows(PasswordResetException.class, () -> passwordResetService.validatePasswordResetToken(testToken.getToken()));
    }

    @Test
    void testValidatePasswordResetToken_NonExistingToken_ThrowsException() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        assertThrows(PasswordResetException.class, () -> passwordResetService.validatePasswordResetToken("invalid-token"));
    }

    @Test
    void testValidatePasswordResetToken_NullToken_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.validatePasswordResetToken(null));
    }

    @Test
    void testValidatePasswordResetToken_EmptyToken_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.validatePasswordResetToken(""));
    }

    @Test
    void testResetPassword_ValidTokenAndPassword_Success() {
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        assertDoesNotThrow(() -> passwordResetService.resetPassword(testToken.getToken(), "newPassword"));
    }

    @Test
    void testResetPassword_ExpiredToken_ThrowsException() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        assertThrows(PasswordResetException.class, () -> passwordResetService.resetPassword(testToken.getToken(), "newPassword"));
    }

    @Test
    void testResetPassword_NullToken_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.resetPassword(null, "newPassword"));
    }

    @Test
    void testResetPassword_EmptyToken_ThrowsException() {
        assertThrows(PasswordResetException.class, () -> passwordResetService.resetPassword("", "newPassword"));
    }

    @Test
    void testResetPassword_NullPassword_ThrowsException() {
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        assertThrows(PasswordResetException.class, () -> passwordResetService.resetPassword(testToken.getToken(), null));
    }

    @Test
    void testResetPassword_EmptyPassword_ThrowsException() {
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(Optional.of(testToken));
        assertThrows(PasswordResetException.class, () -> passwordResetService.resetPassword(testToken.getToken(), ""));
    }
}
