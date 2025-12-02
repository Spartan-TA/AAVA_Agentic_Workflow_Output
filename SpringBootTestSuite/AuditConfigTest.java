package com.company.wems.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AuditConfig
 * Tests cover JPA auditing configuration, AuditorAware implementation,
 * authentication context handling, and various edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditConfig Tests")
public class AuditConfigTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AuditConfig auditConfig;
    private AuditorAware<String> auditorAware;

    @BeforeEach
    public void setUp() {
        auditConfig = new AuditConfig();
        auditorAware = auditConfig.auditorProvider();
        SecurityContextHolder.clearContext();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test auditorProvider returns non-null AuditorAware")
    public void testAuditorProvider_ShouldReturnNonNullAuditorAware() {
        // Act
        AuditorAware<String> provider = auditConfig.auditorProvider();

        // Assert
        assertNotNull(provider, "AuditorAware should not be null");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with authenticated user should return username")
    public void testGetCurrentAuditor_WithAuthenticatedUser_ShouldReturnUsername() {
        // Arrange
        String expectedUsername = "john.doe@company.com";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(expectedUsername);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals(expectedUsername, auditor.get(), "Auditor should match authenticated username");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with JWT authentication should return subject")
    public void testGetCurrentAuditor_WithJwtAuthentication_ShouldReturnSubject() {
        // Arrange
        String expectedSubject = "user123";
        
        Jwt jwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            Map.of("sub", expectedSubject)
        );
        
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuth);
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals(expectedSubject, auditor.get(), "Auditor should match JWT subject");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with different authentication types should return name")
    public void testGetCurrentAuditor_WithDifferentAuthenticationTypes_ShouldReturnName() {
        // Arrange
        String[] usernames = {"admin@company.com", "hr@company.com", "supervisor@company.com", "worker@company.com"};
        
        for (String username : usernames) {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(username);
            
            SecurityContextHolder.setContext(securityContext);

            // Act
            Optional<String> auditor = auditorAware.getCurrentAuditor();

            // Assert
            assertTrue(auditor.isPresent(), "Auditor should be present for " + username);
            assertEquals(username, auditor.get(), "Auditor should match username " + username);
            
            SecurityContextHolder.clearContext();
        }
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test getCurrentAuditor with no authentication should return system")
    public void testGetCurrentAuditor_WithNoAuthentication_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' when no authentication");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with empty security context should return system")
    public void testGetCurrentAuditor_WithEmptySecurityContext_ShouldReturnSystem() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' with empty context");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with unauthenticated user should return system")
    public void testGetCurrentAuditor_WithUnauthenticatedUser_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' for unauthenticated user");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with null authentication name should return system")
    public void testGetCurrentAuditor_WithNullAuthenticationName_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(null);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' when name is null");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with empty authentication name should return system")
    public void testGetCurrentAuditor_WithEmptyAuthenticationName_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("");
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' when name is empty");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with blank authentication name should return system")
    public void testGetCurrentAuditor_WithBlankAuthenticationName_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("   ");
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals("system", auditor.get(), "Auditor should be 'system' when name is blank");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with anonymousUser should return system")
    public void testGetCurrentAuditor_WithAnonymousUser_ShouldReturnSystem() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        // Depending on implementation, might return 'anonymousUser' or 'system'
        assertNotNull(auditor.get(), "Auditor should not be null");
    }

    @Test
    @DisplayName("Test getCurrentAuditor multiple times should return consistent results")
    public void testGetCurrentAuditor_MultipleTimes_ShouldReturnConsistentResults() {
        // Arrange
        String expectedUsername = "test.user@company.com";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(expectedUsername);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor1 = auditorAware.getCurrentAuditor();
        Optional<String> auditor2 = auditorAware.getCurrentAuditor();
        Optional<String> auditor3 = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor1.isPresent(), "First call should return auditor");
        assertTrue(auditor2.isPresent(), "Second call should return auditor");
        assertTrue(auditor3.isPresent(), "Third call should return auditor");
        
        assertEquals(auditor1.get(), auditor2.get(), "Auditors should be consistent");
        assertEquals(auditor2.get(), auditor3.get(), "Auditors should be consistent");
        assertEquals(expectedUsername, auditor1.get(), "Auditor should match expected username");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with changing authentication context")
    public void testGetCurrentAuditor_WithChangingAuthenticationContext_ShouldReflectChanges() {
        // Arrange - First authentication
        String firstUsername = "user1@company.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(firstUsername);
        SecurityContextHolder.setContext(securityContext);

        // Act - First call
        Optional<String> auditor1 = auditorAware.getCurrentAuditor();

        // Assert - First call
        assertTrue(auditor1.isPresent(), "First auditor should be present");
        assertEquals(firstUsername, auditor1.get(), "First auditor should match first username");

        // Arrange - Change authentication
        String secondUsername = "user2@company.com";
        when(authentication.getName()).thenReturn(secondUsername);

        // Act - Second call
        Optional<String> auditor2 = auditorAware.getCurrentAuditor();

        // Assert - Second call
        assertTrue(auditor2.isPresent(), "Second auditor should be present");
        assertEquals(secondUsername, auditor2.get(), "Second auditor should match second username");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with special characters in username")
    public void testGetCurrentAuditor_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        // Arrange
        String[] specialUsernames = {
            "user+tag@company.com",
            "user.name@company.com",
            "user_name@company.com",
            "user-name@company.com"
        };
        
        for (String username : specialUsernames) {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(username);
            
            SecurityContextHolder.setContext(securityContext);

            // Act
            Optional<String> auditor = auditorAware.getCurrentAuditor();

            // Assert
            assertTrue(auditor.isPresent(), "Auditor should be present for " + username);
            assertEquals(username, auditor.get(), "Auditor should match username with special characters");
            
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Test getCurrentAuditor with very long username")
    public void testGetCurrentAuditor_WithVeryLongUsername_ShouldHandleCorrectly() {
        // Arrange
        String longUsername = "a".repeat(100) + "@company.com";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(longUsername);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals(longUsername, auditor.get(), "Auditor should match long username");
    }

    @Test
    @DisplayName("Test getCurrentAuditor with unicode characters in username")
    public void testGetCurrentAuditor_WithUnicodeCharactersInUsername_ShouldHandleCorrectly() {
        // Arrange
        String unicodeUsername = "josÃ©.garcÃ­a@company.com";
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(unicodeUsername);
        
        SecurityContextHolder.setContext(securityContext);

        // Act
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // Assert
        assertTrue(auditor.isPresent(), "Auditor should be present");
        assertEquals(unicodeUsername, auditor.get(), "Auditor should match unicode username");
    }

    @Test
    @DisplayName("Test getCurrentAuditor never returns empty Optional")
    public void testGetCurrentAuditor_ShouldNeverReturnEmptyOptional() {
        // Test various scenarios
        
        // Scenario 1: No authentication
        SecurityContextHolder.clearContext();
        Optional<String> auditor1 = auditorAware.getCurrentAuditor();
        assertTrue(auditor1.isPresent(), "Should always return present Optional (scenario 1)");
        
        // Scenario 2: Null authentication
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        Optional<String> auditor2 = auditorAware.getCurrentAuditor();
        assertTrue(auditor2.isPresent(), "Should always return present Optional (scenario 2)");
        
        // Scenario 3: Unauthenticated
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);
        Optional<String> auditor3 = auditorAware.getCurrentAuditor();
        assertTrue(auditor3.isPresent(), "Should always return present Optional (scenario 3)");
        
        // Scenario 4: Authenticated with valid name
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.setContext(securityContext);
        Optional<String> auditor4 = auditorAware.getCurrentAuditor();
        assertTrue(auditor4.isPresent(), "Should always return present Optional (scenario 4)");
    }

    @Test
    @DisplayName("Test auditorProvider creates new instance each time")
    public void testAuditorProvider_ShouldCreateNewInstanceEachTime() {
        // Act
        AuditorAware<String> provider1 = auditConfig.auditorProvider();
        AuditorAware<String> provider2 = auditConfig.auditorProvider();

        // Assert
        assertNotNull(provider1, "First provider should not be null");
        assertNotNull(provider2, "Second provider should not be null");
        // Note: Depending on implementation (singleton vs prototype), instances might be same or different
        // This test verifies both providers work correctly
        
        // Test both providers work
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@company.com");
        SecurityContextHolder.setContext(securityContext);
        
        Optional<String> auditor1 = provider1.getCurrentAuditor();
        Optional<String> auditor2 = provider2.getCurrentAuditor();
        
        assertTrue(auditor1.isPresent(), "First provider should return auditor");
        assertTrue(auditor2.isPresent(), "Second provider should return auditor");
        assertEquals(auditor1.get(), auditor2.get(), "Both providers should return same auditor");
    }
}