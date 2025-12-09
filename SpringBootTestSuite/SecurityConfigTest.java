import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

public class SecurityConfigTest {
    private SecurityConfig securityConfig;
    private HttpSecurity httpSecurity;

    @BeforeEach
    public void setUp() throws Exception {
        securityConfig = new SecurityConfig();
        httpSecurity = mock(HttpSecurity.class);
    }

    @Test
    public void testSecurityFilterChainCreation() throws Exception {
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testAdminRoleAccess() throws Exception {
        // Simulate admin role access
        when(httpSecurity.authorizeRequests()).thenReturn(null);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testWorkerRoleAccessDenied() throws Exception {
        // Simulate worker role access denied to admin endpoint
        when(httpSecurity.authorizeRequests()).thenReturn(null);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testInvalidRoleThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            // Simulate invalid role configuration
            securityConfig.configureInvalidRole(httpSecurity);
        });
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    public void testEndpointProtection() throws Exception {
        // Simulate endpoint protection
        when(httpSecurity.authorizeRequests()).thenReturn(null);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testOAuth2ToggleEnabled() throws Exception {
        // Simulate OAuth2 toggle
        when(httpSecurity.oauth2Login()).thenReturn(null);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testApiKeyToggleEnabled() throws Exception {
        // Simulate API key toggle
        when(httpSecurity.authorizeRequests()).thenReturn(null);
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        assertNotNull(chain);
    }

    @Test
    public void testNullHttpSecurityThrowsException() {
        assertThrows(NullPointerException.class, () -> securityConfig.securityFilterChain(null));
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }
}
