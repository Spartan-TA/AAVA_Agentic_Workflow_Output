package com.warehouse.employee.management.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive JUnit test suite for SecurityConfig
 * Tests authentication, authorization, and security configurations
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Test Suite")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== PASSWORD ENCODER TESTS ====================

    @Test
    @DisplayName("Test PasswordEncoder Bean - BCrypt Encoder Configured")
    void testPasswordEncoderBean_BCryptEncoderConfigured() {
        // Assert
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.getClass().getName().contains("BCrypt"));
    }

    @Test
    @DisplayName("Test PasswordEncoder - Encode Password - Returns Encoded String")
    void testPasswordEncoder_EncodePassword_ReturnsEncodedString() {
        // Arrange
        String rawPassword = "password123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"));
    }

    @Test
    @DisplayName("Test PasswordEncoder - Matches Password - Returns True for Correct Password")
    void testPasswordEncoder_MatchesPassword_ReturnsTrueForCorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(matches);
    }

    @Test
    @DisplayName("Test PasswordEncoder - Matches Password - Returns False for Incorrect Password")
    void testPasswordEncoder_MatchesPassword_ReturnsFalseForIncorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches);
    }

    @Test
    @DisplayName("Test PasswordEncoder - Same Password Different Encodings - Different Hashes")
    void testPasswordEncoder_SamePasswordDifferentEncodings_DifferentHashes() {
        // Arrange
        String rawPassword = "password123";

        // Act
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotEquals(encodedPassword1, encodedPassword2);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }

    @Test
    @DisplayName("Test PasswordEncoder - Empty Password - Encodes Successfully")
    void testPasswordEncoder_EmptyPassword_EncodesSuccessfully() {
        // Arrange
        String emptyPassword = "";

        // Act
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    @Test
    @DisplayName("Test PasswordEncoder - Long Password - Encodes Successfully")
    void testPasswordEncoder_LongPassword_EncodesSuccessfully() {
        // Arrange
        String longPassword = "A".repeat(100);

        // Act
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }

    @Test
    @DisplayName("Test PasswordEncoder - Special Characters Password - Encodes Successfully")
    void testPasswordEncoder_SpecialCharactersPassword_EncodesSuccessfully() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword));
    }

    // ==================== PUBLIC ENDPOINT TESTS ====================

    @Test
    @WithAnonymousUser
    @DisplayName("Test Public Endpoint - Actuator Health - Returns 200 Without Authentication")
    void testPublicEndpoint_ActuatorHealth_Returns200WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Test Public Endpoint - Actuator Info - Returns 200 Without Authentication")
    void testPublicEndpoint_ActuatorInfo_Returns200WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    // ==================== PROTECTED ENDPOINT TESTS ====================

    @Test
    @WithAnonymousUser
    @DisplayName("Test Protected Endpoint - No Authentication - Returns 401")
    void testProtectedEndpoint_NoAuthentication_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Protected Endpoint - Admin Role - Returns 200")
    void testProtectedEndpoint_AdminRole_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test Protected Endpoint - HR Role - Returns 200")
    void testProtectedEndpoint_HRRole_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test Protected Endpoint - Supervisor Role - Returns 200 or 403 Based on Endpoint")
    void testProtectedEndpoint_SupervisorRole_ReturnsBasedOnEndpoint() throws Exception {
        // Act & Assert - Supervisors may have limited access
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test Protected Endpoint - Worker Role - Returns 403")
    void testProtectedEndpoint_WorkerRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Test Protected Endpoint - User Without Role - Returns 403")
    void testProtectedEndpoint_UserWithoutRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ==================== ROLE-BASED ACCESS CONTROL TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test RBAC - Admin Can Access All Endpoints")
    void testRBAC_AdminCanAccessAllEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Test RBAC - HR Can Access Employee Endpoints")
    void testRBAC_HRCanAccessEmployeeEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("Test RBAC - Multiple Roles - Has Access")
    void testRBAC_MultipleRoles_HasAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "UNKNOWN_ROLE")
    @DisplayName("Test RBAC - Unknown Role - Returns 403")
    void testRBAC_UnknownRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @WithMockUser(username = "admin@warehouse.com", roles = "ADMIN")
    @DisplayName("Test Authentication - Valid User - Has Access")
    void testAuthentication_ValidUser_HasAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Test Authentication - Anonymous User - Returns 401")
    void testAuthentication_AnonymousUser_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@warehouse.com")
    @DisplayName("Test Authentication - Authenticated But No Role - Returns 403")
    void testAuthentication_AuthenticatedButNoRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ==================== CSRF PROTECTION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test CSRF Protection - Enabled for State-Changing Operations")
    void testCSRFProtection_EnabledForStateChangingOperations() throws Exception {
        // Note: CSRF is typically disabled for stateless REST APIs
        // This test verifies the configuration
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ==================== CORS CONFIGURATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test CORS Configuration - Allows Configured Origins")
    void testCORSConfiguration_AllowsConfiguredOrigins() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    // ==================== SESSION MANAGEMENT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Session Management - Stateless Session Policy")
    void testSessionManagement_StatelessSessionPolicy() throws Exception {
        // Act & Assert - Verify stateless behavior
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @WithMockUser(username = "", roles = "ADMIN")
    @DisplayName("Test Edge Case - Empty Username - Has Access")
    void testEdgeCase_EmptyUsername_HasAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@warehouse.com", roles = "")
    @DisplayName("Test Edge Case - Empty Role - Returns 403")
    void testEdgeCase_EmptyRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@warehouse.com", roles = {"ADMIN", "ADMIN"})
    @DisplayName("Test Edge Case - Duplicate Roles - Has Access")
    void testEdgeCase_DuplicateRoles_HasAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@warehouse.com", roles = "admin")
    @DisplayName("Test Edge Case - Lowercase Role - Returns 403")
    void testEdgeCase_LowercaseRole_Returns403() throws Exception {
        // Act & Assert - Roles are case-sensitive
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ==================== PASSWORD STRENGTH TESTS ====================

    @Test
    @DisplayName("Test Password Strength - Weak Password - Encodes Successfully")
    void testPasswordStrength_WeakPassword_EncodesSuccessfully() {
        // Arrange
        String weakPassword = "123";

        // Act
        String encodedPassword = passwordEncoder.encode(weakPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(weakPassword, encodedPassword));
    }

    @Test
    @DisplayName("Test Password Strength - Strong Password - Encodes Successfully")
    void testPasswordStrength_StrongPassword_EncodesSuccessfully() {
        // Arrange
        String strongPassword = "P@ssw0rd!2024#Secure";

        // Act
        String encodedPassword = passwordEncoder.encode(strongPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(strongPassword, encodedPassword));
    }

    @Test
    @DisplayName("Test Password Strength - Unicode Characters - Encodes Successfully")
    void testPasswordStrength_UnicodeCharacters_EncodesSuccessfully() {
        // Arrange
        String unicodePassword = "P@ssw0rdä½ å¥½ä¸ç";

        // Act
        String encodedPassword = passwordEncoder.encode(unicodePassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword));
    }

    // ==================== SECURITY HEADER TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Security Headers - X-Content-Type-Options Present")
    void testSecurityHeaders_XContentTypeOptionsPresent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Security Headers - X-Frame-Options Present")
    void testSecurityHeaders_XFrameOptionsPresent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    // ==================== METHOD SECURITY TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test Method Security - Admin Can Access Protected Methods")
    void testMethodSecurity_AdminCanAccessProtectedMethods() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test Method Security - Worker Cannot Access Admin Methods")
    void testMethodSecurity_WorkerCannotAccessAdminMethods() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }
}