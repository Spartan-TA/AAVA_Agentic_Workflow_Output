import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.*;

public class SecurityConfigTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private SecurityConfig securityConfig;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testLoadUserByUsername_ValidUser() {
        User user = new User("admin", "password", Arrays.asList("ADMIN"));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        UserDetails details = securityConfig.loadUserByUsername("admin");
        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    void testLoadUserByUsername_InvalidUser() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> securityConfig.loadUserByUsername("nouser"));
    }

    @Test
    void testAuthorize_AdminAccess() {
        UserDetails admin = mock(UserDetails.class);
        when(admin.getAuthorities()).thenReturn(Arrays.asList(() -> "ADMIN"));
        assertTrue(securityConfig.isAuthorized(admin, "ADMIN"));
    }

    @Test
    void testAuthorize_ForbiddenAccess() {
        UserDetails worker = mock(UserDetails.class);
        when(worker.getAuthorities()).thenReturn(Arrays.asList(() -> "WORKER"));
        assertFalse(securityConfig.isAuthorized(worker, "ADMIN"));
    }

    @Test
    void testAuthenticate_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> securityConfig.authenticate(null, null));
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User("admin", "password", Arrays.asList("ADMIN"))));
        assertThrows(AuthenticationException.class, () -> securityConfig.authenticate("admin", "wrongpassword"));
    }

    @Test
    void testAuthorize_BoundaryRoles() {
        UserDetails hr = mock(UserDetails.class);
        when(hr.getAuthorities()).thenReturn(Arrays.asList(() -> "HR"));
        assertTrue(securityConfig.isAuthorized(hr, "HR"));
        assertFalse(securityConfig.isAuthorized(hr, "SUPERVISOR"));
    }
}
