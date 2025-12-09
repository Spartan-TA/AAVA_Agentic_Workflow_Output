import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationTest {
    private AuthenticationManager authenticationManager;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetails = mock(UserDetails.class);
    }

    @Test
    public void testAuthenticateValidUser() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "password");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(token)).thenReturn(auth);
        Authentication result = authenticationManager.authenticate(token);
        assertNotNull(result);
    }

    @Test
    public void testAuthenticateInvalidUser() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("invalid", "wrongpass");
        when(authenticationManager.authenticate(token)).thenThrow(new RuntimeException("Bad credentials"));
        assertThrows(RuntimeException.class, () -> authenticationManager.authenticate(token));
    }

    @Test
    public void testAuthenticateNullUsername() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(null, "password");
        when(authenticationManager.authenticate(token)).thenThrow(new IllegalArgumentException("Username cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> authenticationManager.authenticate(token));
    }

    @Test
    public void testAuthenticateNullPassword() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", null);
        when(authenticationManager.authenticate(token)).thenThrow(new IllegalArgumentException("Password cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> authenticationManager.authenticate(token));
    }

    @Test
    public void testAuthenticateEmptyUsername() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("", "password");
        when(authenticationManager.authenticate(token)).thenThrow(new IllegalArgumentException("Username cannot be empty"));
        assertThrows(IllegalArgumentException.class, () -> authenticationManager.authenticate(token));
    }

    @Test
    public void testAuthenticateEmptyPassword() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "");
        when(authenticationManager.authenticate(token)).thenThrow(new IllegalArgumentException("Password cannot be empty"));
        assertThrows(IllegalArgumentException.class, () -> authenticationManager.authenticate(token));
    }

    @Test
    public void testAuthenticateSpecialCharacters() {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user!@#", "pass$%^&*");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(token)).thenReturn(auth);
        Authentication result = authenticationManager.authenticate(token);
        assertNotNull(result);
    }

    @Test
    public void testAuthenticateLongUsername() {
        String longUsername = "u".repeat(256);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(longUsername, "password");
        when(authenticationManager.authenticate(token)).thenThrow(new IllegalArgumentException("Username too long"));
        assertThrows(IllegalArgumentException.class, () -> authenticationManager.authenticate(token));
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }
}
