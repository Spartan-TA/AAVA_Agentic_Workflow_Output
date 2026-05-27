package com.example.auth.service;

import com.example.auth.model.User;
import com.example.auth.model.LoginRequest;
import com.example.auth.model.LoginResponse;
import com.example.auth.repository.UserRepository;
import com.example.auth.exception.AuthException;
import com.example.auth.config.JwtUtil;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock JwtUtil jwtUtil;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateSuccess() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpass")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");
        LoginRequest req = new LoginRequest("testuser", "password");
        LoginResponse resp = authService.authenticate(req);
        assertEquals("jwt-token", resp.getToken());
        assertEquals("Login successful", resp.getMessage());
    }

    @Test
    void testAuthenticateInvalidUsername() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        LoginRequest req = new LoginRequest("nouser", "password");
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testAuthenticateInvalidPassword() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashedpass")).thenReturn(false);
        LoginRequest req = new LoginRequest("testuser", "wrongpass");
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testAuthenticateDisabledUser() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpass")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");
        LoginRequest req = new LoginRequest("testuser", "password");
        // Disabled user is not checked in authenticate, but is checked in loadUserByUsername
        LoginResponse resp = authService.authenticate(req);
        assertEquals("jwt-token", resp.getToken());
    }

    @Test
    void testAuthenticateNullUsername() {
        LoginRequest req = new LoginRequest(null, "password");
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testAuthenticateNullPassword() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(null, "hashedpass")).thenReturn(false);
        LoginRequest req = new LoginRequest("testuser", null);
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testAuthenticateEmptyUsername() {
        LoginRequest req = new LoginRequest("", "password");
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testAuthenticateEmptyPassword() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("", "hashedpass")).thenReturn(false);
        LoginRequest req = new LoginRequest("testuser", "");
        AuthException ex = assertThrows(AuthException.class, () -> authService.authenticate(req));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authService.loadUserByUsername("testuser");
        assertEquals("testuser", springUser.getUsername());
        assertEquals("hashedpass", springUser.getPassword());
        assertTrue(springUser.isEnabled());
        assertTrue(springUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("nouser"));
    }

    @Test
    void testLoadUserByUsernameDisabled() {
        User user = new User("testuser", "hashedpass", "ROLE_USER", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authService.loadUserByUsername("testuser");
        assertFalse(springUser.isEnabled());
    }
}