package com.example.usermanagement.controller;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.service.JwtService;
import com.example.usermanagement.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthController covering registration, login, password reset, and edge cases.
 */
public class AuthControllerTest {
    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_ValidInput_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userService.register(any(User.class))).thenReturn(user);
        ResponseEntity<?> response = authController.register(user);
        assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void testRegister_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authController.register(null));
    }

    @Test
    void testLogin_ValidCredentials_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userService.login(anyString(), anyString())).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt.token");
        ResponseEntity<?> response = authController.login("test@example.com", "Password1");
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("jwt.token"));
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        when(userService.login(anyString(), anyString())).thenThrow(new RuntimeException("Invalid credentials"));
        assertThrows(RuntimeException.class, () -> authController.login("bad@example.com", "badpass"));
    }

    @Test
    void testResetPassword_ValidToken_Success() {
        doNothing().when(userService).resetPassword(anyString(), anyString());
        ResponseEntity<?> response = authController.resetPassword("token", "NewPassword1");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testResetPassword_NullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authController.resetPassword(null, "Password1"));
    }

    @Test
    void testResetPassword_NullPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authController.resetPassword("token", null));
    }
}
