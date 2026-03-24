package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.service.AuthService;
import com.example.auth.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testLogin_ValidCredentials_ReturnsToken() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        LoginResponse response = new LoginResponse("jwt-token");
        when(authService.login(request)).thenReturn(response);
        ResponseEntity<LoginResponse> result = authController.login(request);
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("jwt-token", result.getBody().getToken());
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong");
        when(authService.login(request)).thenThrow(new AuthenticationException("Invalid credentials"));
        assertThrows(AuthenticationException.class, () -> authController.login(request));
    }

    @Test
    void testLogin_NullRequest_ThrowsException() {
        when(authService.login(null)).thenThrow(new AuthenticationException("Null request"));
        assertThrows(AuthenticationException.class, () -> authController.login(null));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        when(authService.validateToken("jwt-token")).thenReturn(true);
        ResponseEntity<Boolean> result = authController.validateToken("jwt-token");
        assertNotNull(result);
        assertTrue(result.getBody());
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        when(authService.validateToken("invalid-token")).thenReturn(false);
        ResponseEntity<Boolean> result = authController.validateToken("invalid-token");
        assertNotNull(result);
        assertFalse(result.getBody());
    }

    @Test
    void testValidateToken_NullToken_ThrowsException() {
        when(authService.validateToken(null)).thenThrow(new IllegalArgumentException("Null token"));
        assertThrows(IllegalArgumentException.class, () -> authController.validateToken(null));
    }
}
