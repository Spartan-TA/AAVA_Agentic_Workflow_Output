package com.example.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.repository.UserRepository;
import com.example.model.User;
import com.example.service.AuthService;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateWithValidCredentials() {
        User user = new User("john", "password123");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = authService.authenticate("john", "password123");
        assertTrue(result);
    }

    @Test
    void testAuthenticateWithInvalidPassword() {
        User user = new User("john", "password123");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = authService.authenticate("john", "wrongpass");
        assertFalse(result);
    }

    @Test
    void testAuthenticateWithNonexistentUser() {
        when(userRepository.findByUsername("jane")).thenReturn(null);
        boolean result = authService.authenticate("jane", "password123");
        assertFalse(result);
    }

    @Test
    void testAuthenticateWithNullUsername() {
        boolean result = authService.authenticate(null, "password123");
        assertFalse(result);
    }

    @Test
    void testAuthenticateWithNullPassword() {
        User user = new User("john", "password123");
        when(userRepository.findByUsername("john")).thenReturn(user);
        boolean result = authService.authenticate("john", null);
        assertFalse(result);
    }

    @Test
    void testAuthenticateWithEmptyCredentials() {
        boolean result = authService.authenticate("", "");
        assertFalse(result);
    }
}