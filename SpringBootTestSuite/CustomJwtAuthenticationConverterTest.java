package com.company.wems.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for CustomJwtAuthenticationConverter
 * Tests cover role extraction from JWT tokens, authority conversion,
 * and various edge cases including null, empty, and multiple roles
 */
@DisplayName("CustomJwtAuthenticationConverter Tests")
public class CustomJwtAuthenticationConverterTest {

    private CustomJwtAuthenticationConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new CustomJwtAuthenticationConverter();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test conversion with single valid role should succeed")
    public void testConvert_WithSingleValidRole_ShouldSucceed() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("roles", Collections.singletonList("admin"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        assertTrue(token instanceof JwtAuthenticationToken, "Token should be JwtAuthenticationToken");
        
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority(), "Authority should be ROLE_ADMIN");
    }

    @Test
    @DisplayName("Test conversion with multiple valid roles should succeed")
    public void testConvert_WithMultipleValidRoles_ShouldSucceed() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user456");
        claims.put("roles", Arrays.asList("admin", "hr", "supervisor"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(3, authorities.size(), "Should have exactly three authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN"), "Should contain ROLE_ADMIN");
        assertTrue(authorityStrings.contains("ROLE_HR"), "Should contain ROLE_HR");
        assertTrue(authorityStrings.contains("ROLE_SUPERVISOR"), "Should contain ROLE_SUPERVISOR");
    }

    @Test
    @DisplayName("Test conversion with lowercase roles should convert to uppercase")
    public void testConvert_WithLowercaseRoles_ShouldConvertToUppercase() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user789");
        claims.put("roles", Arrays.asList("worker", "supervisor"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(2, authorities.size(), "Should have exactly two authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_WORKER"), "Should contain ROLE_WORKER in uppercase");
        assertTrue(authorityStrings.contains("ROLE_SUPERVISOR"), "Should contain ROLE_SUPERVISOR in uppercase");
    }

    @Test
    @DisplayName("Test conversion with mixed case roles should convert to uppercase")
    public void testConvert_WithMixedCaseRoles_ShouldConvertToUppercase() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user101");
        claims.put("roles", Arrays.asList("Admin", "Hr", "SUPERVISOR", "worker"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(4, authorities.size(), "Should have exactly four authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN"), "Should contain ROLE_ADMIN");
        assertTrue(authorityStrings.contains("ROLE_HR"), "Should contain ROLE_HR");
        assertTrue(authorityStrings.contains("ROLE_SUPERVISOR"), "Should contain ROLE_SUPERVISOR");
        assertTrue(authorityStrings.contains("ROLE_WORKER"), "Should contain ROLE_WORKER");
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test conversion with null roles claim should return empty authorities")
    public void testConvert_WithNullRolesClaim_ShouldReturnEmptyAuthorities() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user202");
        claims.put("roles", null);
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.isEmpty(), "Authorities should be empty when roles claim is null");
    }

    @Test
    @DisplayName("Test conversion with missing roles claim should return empty authorities")
    public void testConvert_WithMissingRolesClaim_ShouldReturnEmptyAuthorities() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user303");
        // No roles claim
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.isEmpty(), "Authorities should be empty when roles claim is missing");
    }

    @Test
    @DisplayName("Test conversion with empty roles list should return empty authorities")
    public void testConvert_WithEmptyRolesList_ShouldReturnEmptyAuthorities() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user404");
        claims.put("roles", Collections.emptyList());
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.isEmpty(), "Authorities should be empty when roles list is empty");
    }

    @Test
    @DisplayName("Test conversion with roles as string instead of list should handle gracefully")
    public void testConvert_WithRolesAsString_ShouldHandleGracefully() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user505");
        claims.put("roles", "admin"); // String instead of List
        
        Jwt jwt = createMockJwt(claims);

        // Act & Assert
        assertDoesNotThrow(() -> {
            AbstractAuthenticationToken token = converter.convert(jwt);
            assertNotNull(token, "Token should not be null even with invalid roles format");
        }, "Should handle string roles gracefully without throwing exception");
    }

    @Test
    @DisplayName("Test conversion with duplicate roles should handle correctly")
    public void testConvert_WithDuplicateRoles_ShouldHandleCorrectly() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user606");
        claims.put("roles", Arrays.asList("admin", "admin", "worker", "worker"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        
        // Note: Depending on implementation, duplicates might be preserved or removed
        // This test verifies the converter handles duplicates without errors
        assertTrue(authorities.size() >= 2, "Should have at least 2 authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN"), "Should contain ROLE_ADMIN");
        assertTrue(authorityStrings.contains("ROLE_WORKER"), "Should contain ROLE_WORKER");
    }

    @Test
    @DisplayName("Test conversion with empty string role should handle gracefully")
    public void testConvert_WithEmptyStringRole_ShouldHandleGracefully() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user707");
        claims.put("roles", Arrays.asList("", "admin", "  "));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        
        // Should filter out empty/blank roles
        authorities.forEach(auth -> {
            assertFalse(auth.getAuthority().equals("ROLE_"), "Should not have empty role");
            assertFalse(auth.getAuthority().trim().isEmpty(), "Authority should not be blank");
        });
    }

    @Test
    @DisplayName("Test conversion with special characters in roles should handle correctly")
    public void testConvert_WithSpecialCharactersInRoles_ShouldHandleCorrectly() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user808");
        claims.put("roles", Arrays.asList("admin-user", "hr_manager", "supervisor.lead"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(3, authorities.size(), "Should have exactly three authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN-USER"), "Should contain ROLE_ADMIN-USER");
        assertTrue(authorityStrings.contains("ROLE_HR_MANAGER"), "Should contain ROLE_HR_MANAGER");
        assertTrue(authorityStrings.contains("ROLE_SUPERVISOR.LEAD"), "Should contain ROLE_SUPERVISOR.LEAD");
    }

    @Test
    @DisplayName("Test conversion with numeric roles should handle correctly")
    public void testConvert_WithNumericRoles_ShouldHandleCorrectly() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user909");
        claims.put("roles", Arrays.asList("role1", "role2", "admin123"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(3, authorities.size(), "Should have exactly three authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ROLE1"), "Should contain ROLE_ROLE1");
        assertTrue(authorityStrings.contains("ROLE_ROLE2"), "Should contain ROLE_ROLE2");
        assertTrue(authorityStrings.contains("ROLE_ADMIN123"), "Should contain ROLE_ADMIN123");
    }

    @Test
    @DisplayName("Test conversion with very long role name should handle correctly")
    public void testConvert_WithVeryLongRoleName_ShouldHandleCorrectly() {
        // Arrange
        String longRole = "a".repeat(100);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user1010");
        claims.put("roles", Collections.singletonList(longRole));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        
        GrantedAuthority authority = authorities.iterator().next();
        assertTrue(authority.getAuthority().startsWith("ROLE_"), "Authority should start with ROLE_");
        assertTrue(authority.getAuthority().length() > 100, "Authority should include the long role name");
    }

    @Test
    @DisplayName("Test conversion with all standard WEMS roles should succeed")
    public void testConvert_WithAllStandardWEMSRoles_ShouldSucceed() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user1111");
        claims.put("roles", Arrays.asList("ADMIN", "HR", "SUPERVISOR", "WORKER"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(4, authorities.size(), "Should have exactly four authorities");
        
        Set<String> authorityStrings = new HashSet<>();
        authorities.forEach(auth -> authorityStrings.add(auth.getAuthority()));
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN"), "Should contain ROLE_ADMIN");
        assertTrue(authorityStrings.contains("ROLE_HR"), "Should contain ROLE_HR");
        assertTrue(authorityStrings.contains("ROLE_SUPERVISOR"), "Should contain ROLE_SUPERVISOR");
        assertTrue(authorityStrings.contains("ROLE_WORKER"), "Should contain ROLE_WORKER");
    }

    @Test
    @DisplayName("Test conversion preserves JWT token in authentication")
    public void testConvert_ShouldPreserveJwtTokenInAuthentication() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user1212");
        claims.put("roles", Collections.singletonList("admin"));
        
        Jwt jwt = createMockJwt(claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token, "Token should not be null");
        assertTrue(token instanceof JwtAuthenticationToken, "Token should be JwtAuthenticationToken");
        
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) token;
        assertNotNull(jwtToken.getToken(), "JWT token should be preserved");
        assertEquals(jwt, jwtToken.getToken(), "JWT token should match original");
    }

    @Test
    @DisplayName("Test conversion with null JWT should handle gracefully")
    public void testConvert_WithNullJwt_ShouldHandleGracefully() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            AbstractAuthenticationToken token = converter.convert(null);
            // Depending on implementation, might return null or throw exception
            // This test ensures it doesn't cause unexpected errors
        }, "Should handle null JWT gracefully");
    }

    // ========== HELPER METHODS ==========

    /**
     * Helper method to create a mock JWT with specified claims
     */
    private Jwt createMockJwt(Map<String, Object> claims) {
        return new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }
}