package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testResetPasswordSuccess() {
        User user = new User("john", "oldpass");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = passwordResetService.resetPassword("john", "newpass");
        assertTrue(result);
        verify(userRepository).save(user);
        assertEquals("newpass", user.getPassword());
    }

    @Test
    void testResetPasswordUserNotFound() {
        when(userRepository.findByUsername("jane")).thenReturn(null);
        boolean result = passwordResetService.resetPassword("jane", "newpass");
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testResetPasswordWithNullUsername() {
        boolean result = passwordResetService.resetPassword(null, "newpass");
        assertFalse(result);
    }

    @Test
    void testResetPasswordWithNullPassword() {
        User user = new User("john", "oldpass");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = passwordResetService.resetPassword("john", null);
        assertFalse(result);
    }

    @Test
    void testResetPasswordWithEmptyPassword() {
        User user = new User("john", "oldpass");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = passwordResetService.resetPassword("john", "");
        assertFalse(result);
    }
}