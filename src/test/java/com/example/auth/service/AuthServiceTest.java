package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.exception.AuthenticationException;
import com.example.auth.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private SecurityConfig securityConfig;

    @InjectMocks
    private AuthService authService;

    private AutoCloseable closeable;

    private User testUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRoles(Set.of());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testLoginWithValidCredentials_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(securityConfig.generateJwtToken(testUser)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void testLoginWithInvalidPassword_ThrowsAuthenticationException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testLoginWithNullEmail_ThrowsException() {
        LoginRequest request = new LoginRequest(null, "password");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testLoginWithEmptyPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("test@example.com", "");
        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("", "hashedPassword")).thenReturn(false);
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testLoginWithInvalidEmailFormat_ThrowsException() {
        LoginRequest request = new LoginRequest("invalid-email", "password");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    void testLoginWithNonExistentUser_ThrowsUsernameNotFoundException() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(java.util.Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void testLoginWithNullRequest_ThrowsException() {
        assertThrows(AuthenticationException.class, () -> authService.login(null));
    }

    @Test
    void testJwtTokenGeneration_ValidUser_ReturnsToken() {
        when(securityConfig.generateJwtToken(testUser)).thenReturn("jwt-token");
        String token = authService.generateToken(testUser);
        assertEquals("jwt-token", token);
    }

    @Test
    void testJwtTokenGeneration_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.generateToken(null));
    }

    @Test
    void testPasswordEncoding_ValidPassword_ReturnsEncoded() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        String encoded = authService.encodePassword("password");
        assertEquals("encodedPassword", encoded);
    }

    @Test
    void testPasswordEncoding_NullPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.encodePassword(null));
    }

    @Test
    void testPasswordEncoding_EmptyPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.encodePassword(""));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        when(securityConfig.validateJwtToken("jwt-token")).thenReturn(true);
        assertTrue(authService.validateToken("jwt-token"));
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        when(securityConfig.validateJwtToken("invalid-token")).thenReturn(false);
        assertFalse(authService.validateToken("invalid-token"));
    }

    @Test
    void testValidateToken_NullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.validateToken(null));
    }

    @Test
    void testValidateToken_EmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.validateToken(""));
    }
}