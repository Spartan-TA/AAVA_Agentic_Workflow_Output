package SpringBootTestSuite;

import com.example.app.controller.AuthController;
import com.example.app.entity.User;
import com.example.app.exception.*;
import com.example.app.service.AuthenticationService;
import com.example.app.service.PasswordResetService;
import com.example.app.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setPassword("StrongPass123!");
    }

    @Test
    void testRegisterWithValidInput() {
        when(userService.registerUser(any(User.class))).thenReturn(validUser);
        ResponseEntity<?> response = authController.register(validUser);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testRegisterWithDuplicateEmail() {
        when(userService.registerUser(any(User.class))).thenThrow(UserAlreadyExistsException.class);
        assertThrows(UserAlreadyExistsException.class, () -> authController.register(validUser));
    }

    @Test
    void testRegisterWithWeakPassword() {
        validUser.setPassword("123");
        when(userService.registerUser(any(User.class))).thenThrow(InvalidCredentialsException.class);
        assertThrows(InvalidCredentialsException.class, () -> authController.register(validUser));
    }

    @Test
    void testLoginWithValidCredentials() {
        when(authenticationService.login(validUser.getEmail(), validUser.getPassword())).thenReturn("jwtToken");
        ResponseEntity<?> response = authController.login(validUser.getEmail(), validUser.getPassword());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        when(authenticationService.login(validUser.getEmail(), validUser.getPassword())).thenThrow(InvalidCredentialsException.class);
        assertThrows(InvalidCredentialsException.class, () -> authController.login(validUser.getEmail(), validUser.getPassword()));
    }

    @Test
    void testPasswordResetWithValidToken() {
        doNothing().when(passwordResetService).resetPassword("validToken", "NewPass123!");
        ResponseEntity<?> response = authController.resetPassword("validToken", "NewPass123!");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testPasswordResetWithInvalidToken() {
        doThrow(InvalidTokenException.class).when(passwordResetService).resetPassword("invalidToken", "NewPass123!");
        assertThrows(InvalidTokenException.class, () -> authController.resetPassword("invalidToken", "NewPass123!"));
    }

    @Test
    void testPasswordResetWithExpiredToken() {
        doThrow(TokenExpiredException.class).when(passwordResetService).resetPassword("expiredToken", "NewPass123!");
        assertThrows(TokenExpiredException.class, () -> authController.resetPassword("expiredToken", "NewPass123!"));
    }

    @AfterEach
    void tearDown() {
        validUser = null;
    }
}
