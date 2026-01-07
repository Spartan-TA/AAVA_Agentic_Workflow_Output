package SpringBootTestSuite;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.AccountLockedException;
import com.example.usermanagement.exception.InvalidCredentialsException;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.JwtProvider;
import com.example.usermanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Normal case: Login with valid credentials
    @Test
    void testLogin_WithValidCredentials_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setAccountLocked(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(user)).thenReturn("jwt-token");

        String token = authService.login("test@example.com", "Password123!");
        assertEquals("jwt-token", token);
    }

    // Edge case: Login with invalid password
    @Test
    void testLogin_WithInvalidPassword_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setAccountLocked(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authService.verifyPassword(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login("test@example.com", "wrongPassword"));
    }

    // Edge case: Login with locked account
    @Test
    void testLogin_WithLockedAccount_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setAccountLocked(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class, () -> authService.login("test@example.com", "Password123!"));
    }

    // Boundary case: Login with null email
    @Test
    void testLogin_WithNullEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.login(null, "Password123!"));
    }

    // Boundary case: Login with empty password
    @Test
    void testLogin_WithEmptyPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> authService.login("test@example.com", ""));
    }

    // Edge case: Login with non-existent user
    @Test
    void testLogin_NonExistentUser_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> authService.login("nouser@example.com", "Password123!"));
    }

    // Normal case: Track failed login attempt
    @Test
    void testTrackFailedLoginAttempt_IncrementsCounter() {
        User user = new User();
        user.setFailedLoginAttempts(0);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        authService.trackFailedLoginAttempt("test@example.com");
        assertEquals(1, user.getFailedLoginAttempts());
    }

    // Edge case: Lock account after max failed attempts
    @Test
    void testTrackFailedLoginAttempt_LocksAccount() {
        User user = new User();
        user.setFailedLoginAttempts(4);
        user.setAccountLocked(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        authService.trackFailedLoginAttempt("test@example.com");
        assertTrue(user.isAccountLocked());
    }

    // Normal case: Reset failed login attempts
    @Test
    void testResetFailedLoginAttempts_Success() {
        User user = new User();
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        authService.resetFailedLoginAttempts("test@example.com");
        assertEquals(0, user.getFailedLoginAttempts());
    }

    // Edge case: Reset failed login attempts for non-existent user
    @Test
    void testResetFailedLoginAttempts_NonExistentUser_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> authService.resetFailedLoginAttempts("nouser@example.com"));
    }

    // Normal case: CAPTCHA required after failed attempts
    @Test
    void testIsCaptchaRequired_ReturnsTrueAfterThreshold() {
        User user = new User();
        user.setFailedLoginAttempts(5);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertTrue(authService.isCaptchaRequired("test@example.com"));
    }

    // Normal case: CAPTCHA not required for low attempts
    @Test
    void testIsCaptchaRequired_ReturnsFalseForLowAttempts() {
        User user = new User();
        user.setFailedLoginAttempts(1);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertFalse(authService.isCaptchaRequired("test@example.com"));
    }
}
