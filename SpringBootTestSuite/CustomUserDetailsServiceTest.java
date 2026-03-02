package com.example.usermanagement.security;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CustomUserDetailsService covering user details loading and edge cases.
 */
public class CustomUserDetailsServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_ValidUser_Success() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        UserDetails details = userDetailsService.loadUserByUsername("user@example.com");
        assertNotNull(details);
        assertEquals("user@example.com", details.getUsername());
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("notfound@example.com"));
    }

    @Test
    void testLoadUserByUsername_NullUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userDetailsService.loadUserByUsername(null));
    }

    @Test
    void testLoadUserByUsername_EmptyUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userDetailsService.loadUserByUsername(""));
    }
}
