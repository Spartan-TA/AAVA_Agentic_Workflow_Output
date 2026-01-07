package SpringBootTestSuite;

import com.example.customermanagement.dto.UserRegistrationDto;
import com.example.customermanagement.dto.UserProfileUpdateDto;
import com.example.customermanagement.entity.User;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.exception.UserAlreadyExistsException;
import com.example.customermanagement.repository.UserRepository;
import com.example.customermanagement.service.EmailService;
import com.example.customermanagement.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserService covering registration, verification, profile update, and retrieval.
 */
@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testRegisterUser_WithValidData_ShouldReturnUser() {
        // Arrange
        UserRegistrationDto dto = new UserRegistrationDto("test@example.com", "Password123", "Test User");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    public void testRegisterUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        UserRegistrationDto dto = new UserRegistrationDto("existing@example.com", "Password123", "Test User");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(dto));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    public void testRegisterUser_WithInvalidEmailFormat_ShouldThrowException() {
        // Arrange
        UserRegistrationDto dto = new UserRegistrationDto("invalid-email", "Password123", "Test User");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    public void testRegisterUser_WithWeakPassword_ShouldThrowException() {
        // Arrange
        UserRegistrationDto dto = new UserRegistrationDto("test2@example.com", "weak", "Test User");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    public void testVerifyUser_WithValidToken_ShouldActivateUser() {
        // Arrange
        User user = new User();
        user.setEmail("verify@example.com");
        user.setVerificationToken("valid-token");
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        user.setEnabled(false);
        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userService.verifyUser("valid-token");

        // Assert
        assertTrue(result);
        assertTrue(user.isEnabled());
    }

    @Test
    public void testVerifyUser_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        User user = new User();
        user.setVerificationToken("expired-token");
        user.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1));
        user.setEnabled(false);
        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.verifyUser("expired-token");

        // Assert
        assertFalse(result);
        assertFalse(user.isEnabled());
    }

    @Test
    public void testVerifyUser_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findByVerificationToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        boolean result = userService.verifyUser("invalid-token");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testUpdateProfile_WithValidData_ShouldUpdateUser() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("profile@example.com");
        UserProfileUpdateDto dto = new UserProfileUpdateDto("New Name", "1234567890");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.updateProfile(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("1234567890", result.getPhone());
    }

    @Test
    public void testUpdateProfile_WithInvalidPhoneFormat_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setEmail("profile2@example.com");
        UserProfileUpdateDto dto = new UserProfileUpdateDto("Name", "invalid-phone");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(2L, dto));
    }

    @Test
    public void testUpdateProfile_WithEmptyFields_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setId(3L);
        user.setEmail("profile3@example.com");
        UserProfileUpdateDto dto = new UserProfileUpdateDto("", "");
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(3L, dto));
    }

    @Test
    public void testFindById_WithValidId_ShouldReturnUser() {
        // Arrange
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.findById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    public void testFindById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    public void testFindByEmail_WithValidEmail_ShouldReturnUser() {
        // Arrange
        User user = new User();
        user.setEmail("findme@example.com");
        when(userRepository.findByEmail("findme@example.com")).thenReturn(Optional.of(user));

        // Act
        User result = userService.findByEmail("findme@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("findme@example.com", result.getEmail());
    }

    @Test
    public void testFindByEmail_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail("notfound@example.com"));
    }
}
