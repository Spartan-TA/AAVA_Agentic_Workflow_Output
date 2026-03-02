package com.example.usermanagement.controller;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserController covering profile retrieval, update, and edge cases.
 */
public class UserControllerTest {
    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProfile_ValidUser_Success() {
        User user = new User();
        user.setEmail("profile@example.com");
        when(userService.getProfile(anyString())).thenReturn(user);
        ResponseEntity<?> response = userController.getProfile("profile@example.com");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetProfile_NullEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userController.getProfile(null));
    }

    @Test
    void testUpdateProfile_ValidInput_Success() {
        User user = new User();
        User update = new User();
        update.setEmail("new@example.com");
        when(userService.updateProfile(any(User.class), any(User.class))).thenReturn(update);
        ResponseEntity<?> response = userController.updateProfile(user, update);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(update, response.getBody());
    }

    @Test
    void testUpdateProfile_NullUser_ThrowsException() {
        User update = new User();
        assertThrows(IllegalArgumentException.class, () -> userController.updateProfile(null, update));
    }

    @Test
    void testUpdateProfile_NullUpdate_ThrowsException() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> userController.updateProfile(user, null));
    }
}
