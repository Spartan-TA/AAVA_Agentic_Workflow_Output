package com.warehouse.ems.config;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    SecurityConfig securityConfig;

    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    JwtValidator jwtValidator;

    @Test
    void testRBAC_AdminAccess() {
        UserDetails admin = mock(UserDetails.class);
        when(admin.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(securityConfig.hasAccess(admin, "manageAllRecords"));
    }

    @Test
    void testRBAC_SupervisorLimitedAccess() {
        UserDetails supervisor = mock(UserDetails.class);
        when(supervisor.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));
        assertTrue(securityConfig.hasAccess(supervisor, "manageTeamRecords"));
        assertFalse(securityConfig.hasAccess(supervisor, "manageAllRecords"));
    }

    @Test
    void testRBAC_WorkerNoSensitiveAccess() {
        UserDetails worker = mock(UserDetails.class);
        when(worker.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_WORKER")));
        assertFalse(securityConfig.hasAccess(worker, "manageAllRecords"));
    }

    @Test
    void test401Unauthorized() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        assertEquals(401, securityConfig.checkAccess(auth, "anyAction"));
    }

    @Test
    void test403Forbidden() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_WORKER")));
        assertEquals(403, securityConfig.checkAccess(auth, "manageAllRecords"));
    }

    @Test
    void testRoleHierarchy() {
        assertTrue(securityConfig.isRoleHigher("ADMIN", "SUPERVISOR"));
        assertFalse(securityConfig.isRoleHigher("WORKER", "HR"));
    }

    @Test
    void testJWTValidation_Success() {
        String jwt = "valid.jwt.token";
        when(jwtValidator.validate(jwt)).thenReturn(true);
        assertTrue(securityConfig.validateJWT(jwt));
    }

    @Test
    void testJWTValidation_Failure() {
        String jwt = "invalid.jwt.token";
        when(jwtValidator.validate(jwt)).thenReturn(false);
        assertFalse(securityConfig.validateJWT(jwt));
    }

    @Test
    void testNullJWT_Throws() {
        assertThrows(IllegalArgumentException.class, () -> securityConfig.validateJWT(null));
    }

    @Test
    void testIntegration_MultipleRoles() {
        UserDetails admin = mock(UserDetails.class);
        UserDetails hr = mock(UserDetails.class);
        when(admin.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(hr.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_HR")));
        assertTrue(securityConfig.hasAccess(admin, "manageAllRecords"));
        assertTrue(securityConfig.hasAccess(hr, "manageHRRecords"));
    }
}
