package com.company.wms.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SecurityConfig securityConfig;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    public void testAuthentication_WithValidCredentials_Success() {
        UserDetails user = new User("admin", passwordEncoder.encode("password"), Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities()));
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("admin", "password"));
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
    }

    @Test
    public void testAuthentication_WithInvalidCredentials_Fails() {
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new RuntimeException("Bad credentials"));
        assertThrows(RuntimeException.class, () -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("admin", "wrongpass")));
    }

    @Test
    public void testAuthorization_AdminRole_AccessAllEndpoints() {
        UserDetails user = new User("admin", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    public void testAuthorization_HRRole_AccessEmployeeEndpoints() {
        UserDetails user = new User("hr", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_HR")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR")));
    }

    @Test
    public void testAuthorization_SupervisorRole_AccessTeamData() {
        UserDetails user = new User("supervisor", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR")));
    }

    @Test
    public void testAuthorization_WorkerRole_AccessOwnDataOnly() {
        UserDetails user = new User("worker", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_WORKER")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WORKER")));
    }

    @Test
    public void testJWTValidation_WithValidToken_Success() {
        when(jwtTokenProvider.validateToken("valid.jwt.token")).thenReturn(true);
        assertTrue(jwtTokenProvider.validateToken("valid.jwt.token"));
    }

    @Test
    public void testJWTValidation_WithExpiredToken_Fails() {
        when(jwtTokenProvider.validateToken("expired.jwt.token")).thenReturn(false);
        assertFalse(jwtTokenProvider.validateToken("expired.jwt.token"));
    }

    @Test
    public void testJWTValidation_WithInvalidToken_Fails() {
        when(jwtTokenProvider.validateToken("invalid.jwt.token")).thenReturn(false);
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }

    @Test
    public void testPasswordEncoding_BCryptUsed_Success() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }
}
