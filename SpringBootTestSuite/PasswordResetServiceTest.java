package SpringBootTestSuite;

import com.example.app.entity.PasswordResetToken;
import com.example.app.entity.User;
import com.example.app.exception.InvalidTokenException;
import com.example.app.exception.TokenExpiredException;
import com.example.app.repository.PasswordResetTokenRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.PasswordResetService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private PasswordResetToken validToken;
    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setPassword("StrongPass123!");

        validToken = new PasswordResetToken();
        validToken.setToken("validToken");
        validToken.setUser(validUser);
        validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
    }

    @Test
    void testResetPasswordWithValidToken() {
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("NewPass123!"))
                .thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        passwordResetService.resetPassword("validToken", "NewPass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPasswordWithInvalidToken() {
        when(tokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword("invalidToken", "NewPass123!"));
    }

    @Test
    void testResetPasswordWithExpiredToken() {
        validToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(validToken));
        assertThrows(TokenExpiredException.class, () -> passwordResetService.resetPassword("validToken", "NewPass123!"));
    }

    @Test
    void testResetPasswordWithNullToken() {
        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(null, "NewPass123!"));
    }

    @Test
    void testResetPasswordWithEmptyToken() {
        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword("", "NewPass123!"));
    }

    @Test
    void testResetPasswordWithNullPassword() {
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(validToken));
        assertThrows(NullPointerException.class, () -> passwordResetService.resetPassword("validToken", null));
    }

    @Test
    void testResetPasswordWithEmptyPassword() {
        when(tokenRepository.findByToken("validToken")).thenReturn(Optional.of(validToken));
        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword("validToken", ""));
    }

    @AfterEach
    void tearDown() {
        validToken = null;
        validUser = null;
    }
}
