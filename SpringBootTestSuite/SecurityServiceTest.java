package com.example.warehouse.test;

import com.example.warehouse.security.SecurityService;
import com.example.warehouse.security.SecurityController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @InjectMocks
    private SecurityService securityService;
    private SecurityController securityController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityController = new SecurityController(securityService);
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testAuthenticate_ValidCredentials_Success() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        assertDoesNotThrow(() -> securityService.authenticate("user", "pass"));
    }

    @Test
    void testAuthenticate_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any())).thenThrow(new UsernameNotFoundException("Bad credentials"));
        assertThrows(UsernameNotFoundException.class, () -> securityService.authenticate("user", "wrong"));
    }

    @Test
    void testLoadUserByUsername_UserExists_Success() {
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(mock(org.springframework.security.core.userdetails.UserDetails.class));
        assertNotNull(securityService.loadUserByUsername("admin"));
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        when(userDetailsService.loadUserByUsername("ghost")).thenThrow(new UsernameNotFoundException("Not found"));
        assertThrows(UsernameNotFoundException.class, () -> securityService.loadUserByUsername("ghost"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAdminAccess_Allowed() {
        assertTrue(securityService.hasRole("ADMIN"));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    void testAdminAccess_Forbidden() {
        assertFalse(securityService.hasRole("ADMIN"));
    }

    @Test
    void testController_Authenticate_Success() {
        when(securityService.authenticate(anyString(), anyString())).thenReturn("jwt-token");
        ResponseEntity<String> response = securityController.authenticate("user", "pass");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("jwt-token", response.getBody());
    }

    @Test
    void testController_Authenticate_Failure() {
        when(securityService.authenticate(anyString(), anyString())).thenThrow(new UsernameNotFoundException("Bad credentials"));
        assertThrows(UsernameNotFoundException.class, () -> securityController.authenticate("user", "bad"));
    }
}
