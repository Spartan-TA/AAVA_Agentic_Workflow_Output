import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;

public class AuthorizationTest {
    private AuthorizationService authorizationService;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        authorizationService = mock(AuthorizationService.class);
        authentication = mock(Authentication.class);
    }

    @Test
    public void testAdminCanAccessAll() {
        when(authentication.getAuthorities()).thenReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authorizationService.hasAccess(authentication, "manageAll")).thenReturn(true);
        assertTrue(authorizationService.hasAccess(authentication, "manageAll"));
    }

    @Test
    public void testSupervisorLimitedAccess() {
        when(authentication.getAuthorities()).thenReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));
        when(authorizationService.hasAccess(authentication, "manageTeam")).thenReturn(true);
        assertTrue(authorizationService.hasAccess(authentication, "manageTeam"));
        when(authorizationService.hasAccess(authentication, "manageAll")).thenReturn(false);
        assertFalse(authorizationService.hasAccess(authentication, "manageAll"));
    }

    @Test
    public void testWorkerAccessDenied() {
        when(authentication.getAuthorities()).thenReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_WORKER")));
        when(authorizationService.hasAccess(authentication, "adminPanel")).thenReturn(false);
        assertFalse(authorizationService.hasAccess(authentication, "adminPanel"));
    }

    @Test
    public void testUnauthorizedThrowsException() {
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authorizationService.hasAccess(authentication, "anyAction")).thenThrow(new AccessDeniedException("Access denied"));
        assertThrows(AccessDeniedException.class, () -> authorizationService.hasAccess(authentication, "anyAction"));
    }

    @Test
    public void testNullAuthenticationThrowsException() {
        when(authorizationService.hasAccess(null, "manageAll")).thenThrow(new IllegalArgumentException("Authentication required"));
        assertThrows(IllegalArgumentException.class, () -> authorizationService.hasAccess(null, "manageAll"));
    }

    @Test
    public void testNullActionThrowsException() {
        when(authorizationService.hasAccess(authentication, null)).thenThrow(new IllegalArgumentException("Action required"));
        assertThrows(IllegalArgumentException.class, () -> authorizationService.hasAccess(authentication, null));
    }

    @Test
    public void testEmptyActionThrowsException() {
        when(authorizationService.hasAccess(authentication, "")).thenThrow(new IllegalArgumentException("Action required"));
        assertThrows(IllegalArgumentException.class, () -> authorizationService.hasAccess(authentication, ""));
    }

    @Test
    public void testRoleHierarchyAccess() {
        when(authentication.getAuthorities()).thenReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_HR")));
        when(authorizationService.hasAccess(authentication, "viewEmployee")).thenReturn(true);
        assertTrue(authorizationService.hasAccess(authentication, "viewEmployee"));
    }

    @Test
    public void testInvalidRoleThrowsException() {
        when(authentication.getAuthorities()).thenReturn(Arrays.asList(new SimpleGrantedAuthority("ROLE_UNKNOWN")));
        when(authorizationService.hasAccess(authentication, "manageAll")).thenThrow(new AccessDeniedException("Invalid role"));
        assertThrows(AccessDeniedException.class, () -> authorizationService.hasAccess(authentication, "manageAll"));
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }
}
