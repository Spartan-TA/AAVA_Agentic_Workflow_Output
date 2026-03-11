package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {
        @Test
        void login_withValidCredentials_returnsToken() {
            LoginRequest request = new LoginRequest("user1", "password123");
            Authentication authentication = Mockito.mock(Authentication.class);
            Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            Mockito.when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

            LoginResponse response = authService.login(request);
            assertEquals("jwt-token", response.getToken());
        }

        @Test
        void login_withInvalidCredentials_throwsException() {
            LoginRequest request = new LoginRequest("user1", "wrongpass");
            Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));
            assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        }
    }

    @Nested
    @DisplayName("Registration")
    class RegistrationTests {
        @Test
        void register_withValidInput_returnsUser() {
            RegisterRequest request = new RegisterRequest("user2", "user2@email.com", "Password1!");
            Mockito.when(userRepository.existsByUsername("user2")).thenReturn(false);
            Mockito.when(userRepository.existsByEmail("user2@email.com")).thenReturn(false);
            Mockito.when(passwordEncoder.encode("Password1!")).thenReturn("encodedPass");
            User user = new User();
            user.setUsername("user2");
            user.setEmail("user2@email.com");
            Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

            RegisterResponse response = authService.register(request);
            assertEquals("user2", response.getUsername());
            assertEquals("user2@email.com", response.getEmail());
        }

        @Test
        void register_withExistingUsername_throwsException() {
            RegisterRequest request = new RegisterRequest("user1", "user1@email.com", "Password1!");
            Mockito.when(userRepository.existsByUsername("user1")).thenReturn(true);
            assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        }

        @Test
        void register_withExistingEmail_throwsException() {
            RegisterRequest request = new RegisterRequest("user3", "user3@email.com", "Password1!");
            Mockito.when(userRepository.existsByUsername("user3")).thenReturn(false);
            Mockito.when(userRepository.existsByEmail("user3@email.com")).thenReturn(true);
            assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        }
    }

    @Nested
    @DisplayName("Password Reset")
    class PasswordResetTests {
        @Test
        void resetPassword_withValidEmail_succeeds() {
            PasswordResetRequest request = new PasswordResetRequest("user1@email.com");
            User user = new User();
            user.setEmail("user1@email.com");
            Mockito.when(userRepository.findByEmail("user1@email.com")).thenReturn(Optional.of(user));
            assertDoesNotThrow(() -> authService.resetPassword(request));
        }

        @Test
        void resetPassword_withNonexistentEmail_throwsException() {
            PasswordResetRequest request = new PasswordResetRequest("notfound@email.com");
            Mockito.when(userRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> authService.resetPassword(request));
        }
    }
}
