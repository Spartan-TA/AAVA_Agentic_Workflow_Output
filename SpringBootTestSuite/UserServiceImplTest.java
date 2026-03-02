package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.DuplicateEmailException;
import com.example.usermanagement.exception.InvalidPasswordException;
import com.example.usermanagement.exception.UserNotFoundException;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.impl.UserServiceImpl;
import com.example.usermanagement.service.VerificationTokenService;
import com.example.usermanagement.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserServiceImpl covering normal and edge cases.
 */
public class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificationTokenService tokenService;
    @Mock private EmailService emailService;
    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_ValidInput_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("Password1");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.register(user);
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(emailService, times(1)).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        assertThrows(DuplicateEmailException.class, () -> userService.register(user));
    }

    @Test
    void testRegister_InvalidPassword_ThrowsException() {
        User user = new User();
        user.setEmail("test2@example.com");
        user.setPassword("short");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidPasswordException.class, () -> userService.register(user));
    }

    @Test
    void testRegister_NullEmail_ThrowsException() {
        User user = new User();
        user.setEmail(null);
        user.setPassword("Password1");
        assertThrows(IllegalArgumentException.class, () -> userService.register(user));
    }

    @Test
    void testLogin_ValidCredentials_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setAccountLocked(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        User result = userService.login("test@example.com", "Password1");
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testLogin_InvalidPassword_IncrementsFailedAttempts() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(InvalidPasswordException.class, () -> userService.login("test@example.com", "wrong"));
        assertEquals(4, user.getFailedLoginAttempts());
    }

    @Test
    void testLogin_AccountLocked_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setAccountLocked(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        assertThrows(UserNotFoundException.class, () -> userService.login("test@example.com", "Password1"));
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.login("notfound@example.com", "Password1"));
    }

    @Test
    void testResetPassword_ValidToken_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(tokenService.validatePasswordResetToken(anyString())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        userService.resetPassword("validToken", "NewPassword1");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetPassword_InvalidToken_ThrowsException() {
        when(tokenService.validatePasswordResetToken(anyString())).thenThrow(new IllegalArgumentException("Invalid token"));
        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword("badToken", "Password1"));
    }

    @Test
    void testResetPassword_InvalidPassword_ThrowsException() {
        User user = new User();
        when(tokenService.validatePasswordResetToken(anyString())).thenReturn(user);
        assertThrows(InvalidPasswordException.class, () -> userService.resetPassword("validToken", "short"));
    }

    @Test
    void testUpdateProfile_EmailChanged_TriggersReverification() {
        User user = new User();
        user.setEmail("old@example.com");
        User updated = new User();
        updated.setEmail("new@example.com");
        when(userRepository.save(any(User.class))).thenReturn(updated);
        userService.updateProfile(user, updated);
        verify(emailService, times(1)).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void testUpdateProfile_NullUser_ThrowsException() {
        User updated = new User();
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(null, updated));
    }

    @Test
    void testUpdateProfile_NullUpdate_ThrowsException() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(user, null));
    }
}
