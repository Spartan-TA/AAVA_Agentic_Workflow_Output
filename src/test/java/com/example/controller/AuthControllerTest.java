package com.example.controller;

import com.example.model.User;
import com.example.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        when(authService.authenticate("john", "pass")).thenReturn(true);
        ResponseEntity<String> response = authController.login("john", "pass");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
    }

    @Test
    void testLoginFailure() {
        when(authService.authenticate("john", "wrong")).thenReturn(false);
        ResponseEntity<String> response = authController.login("john", "wrong");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void testLoginWithNullUsername() {
        ResponseEntity<String> response = authController.login(null, "pass");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLoginWithNullPassword() {
        ResponseEntity<String> response = authController.login("john", null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLogout() {
        ResponseEntity<String> response = authController.logout();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out", response.getBody());
    }
}
