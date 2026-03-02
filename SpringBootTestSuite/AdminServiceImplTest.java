package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.UserNotFoundException;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AdminServiceImpl covering admin operations and edge cases.
 */
public class AdminServiceImplTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testViewAllUsers_ReturnsUserList() {
        User user1 = new User(); user1.setEmail("user1@example.com");
        User user2 = new User(); user2.setEmail("user2@example.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<User> users = adminService.viewAllUsers();
        assertEquals(2, users.size());
        assertEquals("user1@example.com", users.get(0).getEmail());
    }

    @Test
    void testViewAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        List<User> users = adminService.viewAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void testDeactivateUser_ValidUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        adminService.deactivateUser(1L);
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeactivateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.deactivateUser(2L));
    }

    @Test
    void testActivateUser_ValidUser_Success() {
        User user = new User();
        user.setId(3L);
        user.setActive(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        adminService.activateUser(3L);
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testActivateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> adminService.activateUser(4L));
    }

    @Test
    void testDeactivateUser_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adminService.deactivateUser(null));
    }

    @Test
    void testActivateUser_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> adminService.activateUser(null));
    }
}
