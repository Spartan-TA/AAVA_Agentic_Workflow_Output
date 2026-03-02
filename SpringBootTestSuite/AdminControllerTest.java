package com.example.usermanagement.controller;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.AdminService;
import com.example.usermanagement.controller.AdminController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AdminController covering user management endpoints and edge cases.
 */
public class AdminControllerTest {
    @Mock private AdminService adminService;
    @InjectMocks private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testViewAllUsers_Success() {
        User user1 = new User(); user1.setEmail("a@example.com");
        User user2 = new User(); user2.setEmail("b@example.com");
        when(adminService.viewAllUsers()).thenReturn(Arrays.asList(user1, user2));
        ResponseEntity<?> response = adminController.viewAllUsers();
        assertEquals(200, response.getStatusCodeValue());
        List<User> users = (List<User>) response.getBody();
        assertEquals(2, users.size());
    }

    @Test
    void testDeactivateUser_ValidId_Success() {
        doNothing().when(adminService).deactivateUser(1L);
        ResponseEntity<?> response = adminController.deactivateUser(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeactivateUser_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adminController.deactivateUser(null));
    }

    @Test
    void testActivateUser_ValidId_Success() {
        doNothing().when(adminService).activateUser(2L);
        ResponseEntity<?> response = adminController.activateUser(2L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testActivateUser_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adminController.activateUser(null));
    }
}
