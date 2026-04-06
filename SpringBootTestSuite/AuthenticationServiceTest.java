package SpringBootTestSuite;

import com.example.app.entity.User;
import com.example.app.exception.*;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtTokenProvider;
import com.example.app.service.AuthenticationService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setPassword("StrongPass123!");
        validUser.setAccountLocked(false);
        validUser.setFailedLoginAttempts(0);
    }

    @Test
    void testLoginWithValidCredentials() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches("StrongPass123!", validUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(validUser)).thenReturn("jwtToken");
        String token = authenticationService.login(validUser.getEmail(), "StrongPass123!");
        assertEquals("jwtToken", token);
    }

    @Test
    void testLoginWithInvalidPassword() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches("WrongPass", validUser.getPassword())).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(validUser.getEmail(), "WrongPass"));
    }

    @Test
    void testLoginWithAccountLocked() {
        validUser.setAccountLocked(true);
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        assertThrows(AccountLockedException.class, () -> authenticationService.login(validUser.getEmail(), "StrongPass123!"));
    }

    @Test
    void testLoginWithNonExistentUser() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authenticationService.login(validUser.getEmail(), "StrongPass123!"));
    }

    @Test
    void testAccountLockoutAfterFiveFailedAttempts() {
        validUser.setFailedLoginAttempts(5);
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        assertThrows(AccountLockedException.class, () -> authenticationService.login(validUser.getEmail(), "StrongPass123!"));
    }

    @Test
    void testJwtTokenGeneration() {
        when(jwtTokenProvider.generateToken(validUser)).thenReturn("jwtToken");
        String token = jwtTokenProvider.generateToken(validUser);
        assertEquals("jwtToken", token);
    }

    @Test
    void testJwtTokenValidationValid() {
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(true);
        assertTrue(jwtTokenProvider.validateToken("validToken"));
    }

    @Test
    void testJwtTokenValidationInvalid() {
        when(jwtTokenProvider.validateToken("invalidToken")).thenReturn(false);
        assertFalse(jwtTokenProvider.validateToken("invalidToken"));
    }

    @Test
    void testJwtTokenValidationExpired() {
        when(jwtTokenProvider.validateToken("expiredToken")).thenThrow(TokenExpiredException.class);
        assertThrows(TokenExpiredException.class, () -> jwtTokenProvider.validateToken("expiredToken"));
    }

    @Test
    void testLoginWithNullEmail() {
        assertThrows(NullPointerException.class, () -> authenticationService.login(null, "StrongPass123!"));
    }

    @Test
    void testLoginWithEmptyEmail() {
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login("", "StrongPass123!"));
    }

    @AfterEach
    void tearDown() {
        validUser = null;
    }
}
