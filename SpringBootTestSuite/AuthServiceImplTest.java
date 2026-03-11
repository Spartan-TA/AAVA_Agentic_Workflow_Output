package SpringBootTestSuite;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthServiceImpl;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.exception.RegistrationException;
import com.example.demo.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
    }

    @Test
    void testRegisterUser_HappyPath() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User registered = authService.registerUser(user);
        assertEquals("testuser", registered.getUsername());
    }

    @Test
    void testRegisterUser_ExistingEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        assertThrows(RegistrationException.class, () -> authService.registerUser(user));
    }

    @Test
    void testAuthenticateUser_HappyPath() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(java.util.Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn("token");
        String token = authService.authenticateUser(user.getUsername(), user.getPassword());
        assertEquals("token", token);
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(java.util.Optional.empty());
        assertThrows(RegistrationException.class, () -> authService.authenticateUser(user.getUsername(), user.getPassword()));
    }

    @Test
    void testAuthenticateUser_NullUsername() {
        assertThrows(IllegalArgumentException.class, () -> authService.authenticateUser(null, "password"));
    }

    @Test
    void testAuthenticateUser_EmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> authService.authenticateUser("testuser", ""));
    }

    @Test
    void testValidateToken_HappyPath() {
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(true);
        assertTrue(authService.validateToken("validToken"));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        when(jwtTokenProvider.validateToken("expiredToken")).thenThrow(new TokenExpiredException("Token expired"));
        assertThrows(TokenExpiredException.class, () -> authService.validateToken("expiredToken"));
    }

    @Test
    void testValidateToken_NullToken() {
        assertThrows(IllegalArgumentException.class, () -> authService.validateToken(null));
    }
}