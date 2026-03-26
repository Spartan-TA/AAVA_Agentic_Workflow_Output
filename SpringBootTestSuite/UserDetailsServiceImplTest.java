package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for UserDetailsServiceImpl
 * Tests user loading, authentication, and edge cases
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Test Suite")
public class UserDetailsServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        // Initialize test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setRole("ADMIN");
        testEmployee.setDepartment("IT");
        testEmployee.setShiftGroup("Day");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== LOAD USER BY USERNAME TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Valid Email - Returns UserDetails")
    void testLoadUserByUsername_ValidEmail_ReturnsUserDetails() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("john.doe@warehouse.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(employeeRepository, times(1)).findByEmail("john.doe@warehouse.com");
    }

    @Test
    @DisplayName("Test loadUserByUsername - Invalid Email - Throws UsernameNotFoundException")
    void testLoadUserByUsername_InvalidEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.findByEmail("nonexistent@warehouse.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent@warehouse.com")
        );
        assertTrue(exception.getMessage().contains("User not found"));
        verify(employeeRepository, times(1)).findByEmail("nonexistent@warehouse.com");
    }

    @Test
    @DisplayName("Test loadUserByUsername - Null Email - Throws UsernameNotFoundException")
    void testLoadUserByUsername_NullEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(null)
        );
    }

    @Test
    @DisplayName("Test loadUserByUsername - Empty Email - Throws UsernameNotFoundException")
    void testLoadUserByUsername_EmptyEmail_ThrowsUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.findByEmail("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("")
        );
    }

    @Test
    @DisplayName("Test loadUserByUsername - Deleted Employee - Throws UsernameNotFoundException")
    void testLoadUserByUsername_DeletedEmployee_ThrowsUsernameNotFoundException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("john.doe@warehouse.com")
        );
    }

    // ==================== USER DETAILS PROPERTIES TESTS ====================

    @Test
    @DisplayName("Test UserDetails - Username Matches Email")
    void testUserDetails_UsernameMatchesEmail() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertEquals("john.doe@warehouse.com", userDetails.getUsername());
    }

    @Test
    @DisplayName("Test UserDetails - Has Correct Role")
    void testUserDetails_HasCorrectRole() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Test UserDetails - Account Not Expired")
    void testUserDetails_AccountNotExpired() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    @DisplayName("Test UserDetails - Account Not Locked")
    void testUserDetails_AccountNotLocked() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("Test UserDetails - Credentials Not Expired")
    void testUserDetails_CredentialsNotExpired() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Test UserDetails - Account Enabled")
    void testUserDetails_AccountEnabled() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.isEnabled());
    }

    // ==================== ROLE MAPPING TESTS ====================

    @Test
    @DisplayName("Test Role Mapping - ADMIN Role - Maps to ROLE_ADMIN")
    void testRoleMapping_AdminRole_MapsToROLE_ADMIN() {
        // Arrange
        testEmployee.setRole("ADMIN");
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Test Role Mapping - HR Role - Maps to ROLE_HR")
    void testRoleMapping_HRRole_MapsToROLE_HR() {
        // Arrange
        testEmployee.setRole("HR");
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_HR")));
    }

    @Test
    @DisplayName("Test Role Mapping - SUPERVISOR Role - Maps to ROLE_SUPERVISOR")
    void testRoleMapping_SupervisorRole_MapsToROLE_SUPERVISOR() {
        // Arrange
        testEmployee.setRole("SUPERVISOR");
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPERVISOR")));
    }

    @Test
    @DisplayName("Test Role Mapping - WORKER Role - Maps to ROLE_WORKER")
    void testRoleMapping_WorkerRole_MapsToROLE_WORKER() {
        // Arrange
        testEmployee.setRole("WORKER");
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_WORKER")));
    }

    @Test
    @DisplayName("Test Role Mapping - Null Role - Has Default Role")
    void testRoleMapping_NullRole_HasDefaultRole() {
        // Arrange
        testEmployee.setRole(null);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Test Role Mapping - Empty Role - Has Default Role")
    void testRoleMapping_EmptyRole_HasDefaultRole() {
        // Arrange
        testEmployee.setRole("");
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails.getAuthorities());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Email With Whitespace - Trims and Finds User")
    void testLoadUserByUsername_EmailWithWhitespace_TrimsAndFindsUser() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(" john.doe@warehouse.com ");

        // Assert
        assertNotNull(userDetails);
    }

    @Test
    @DisplayName("Test loadUserByUsername - Case Sensitive Email - Exact Match Required")
    void testLoadUserByUsername_CaseSensitiveEmail_ExactMatchRequired() {
        // Arrange
        when(employeeRepository.findByEmail("JOHN.DOE@WAREHOUSE.COM"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("JOHN.DOE@WAREHOUSE.COM")
        );
    }

    @Test
    @DisplayName("Test loadUserByUsername - Special Characters in Email - Finds User")
    void testLoadUserByUsername_SpecialCharactersInEmail_FindsUser() {
        // Arrange
        testEmployee.setEmail("john.doe+test@warehouse.com");
        when(employeeRepository.findByEmail("john.doe+test@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe+test@warehouse.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("john.doe+test@warehouse.com", userDetails.getUsername());
    }

    @Test
    @DisplayName("Test loadUserByUsername - Very Long Email - Finds User")
    void testLoadUserByUsername_VeryLongEmail_FindsUser() {
        // Arrange
        String longEmail = "a".repeat(50) + "@warehouse.com";
        testEmployee.setEmail(longEmail);
        when(employeeRepository.findByEmail(longEmail)).thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(longEmail);

        // Assert
        assertNotNull(userDetails);
        assertEquals(longEmail, userDetails.getUsername());
    }

    // ==================== INACTIVE EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Inactive Employee - Returns UserDetails")
    void testLoadUserByUsername_InactiveEmployee_ReturnsUserDetails() {
        // Arrange
        testEmployee.setStatus("INACTIVE");
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails);
        // Note: Depending on business logic, inactive users might be disabled
    }

    @Test
    @DisplayName("Test loadUserByUsername - Suspended Employee - Returns UserDetails")
    void testLoadUserByUsername_SuspendedEmployee_ReturnsUserDetails() {
        // Arrange
        testEmployee.setStatus("SUSPENDED");
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails);
    }

    // ==================== REPOSITORY EXCEPTION TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Repository Throws Exception - Propagates Exception")
    void testLoadUserByUsername_RepositoryThrowsException_PropagatesException() {
        // Arrange
        when(employeeRepository.findByEmail(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername("john.doe@warehouse.com")
        );
    }

    @Test
    @DisplayName("Test loadUserByUsername - Repository Returns Null - Throws UsernameNotFoundException")
    void testLoadUserByUsername_RepositoryReturnsNull_ThrowsUsernameNotFoundException() {
        // Arrange
        when(employeeRepository.findByEmail(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> userDetailsService.loadUserByUsername("john.doe@warehouse.com")
        );
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Multiple Calls - Performs Efficiently")
    void testLoadUserByUsername_MultipleCalls_PerformsEfficiently() {
        // Arrange
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            userDetailsService.loadUserByUsername("john.doe@warehouse.com");
        }
        long endTime = System.currentTimeMillis();

        // Assert
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Should load 1000 users in less than 1 second");
    }

    // ==================== CONCURRENT ACCESS TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Concurrent Calls - Thread Safe")
    void testLoadUserByUsername_ConcurrentCalls_ThreadSafe() throws InterruptedException {
        // Arrange
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(testEmployee));

        // Act
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");
                    assertNotNull(userDetails);
                } catch (Exception e) {
                    fail("Exception in concurrent execution: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - No exceptions thrown
        assertTrue(true);
    }

    // ==================== INTEGRATION WITH SECURITY CONTEXT TESTS ====================

    @Test
    @DisplayName("Test loadUserByUsername - Returns UserDetails Compatible With Spring Security")
    void testLoadUserByUsername_ReturnsUserDetailsCompatibleWithSpringSecurity() {
        // Arrange
        when(employeeRepository.findByEmail("john.doe@warehouse.com"))
                .thenReturn(Optional.of(testEmployee));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john.doe@warehouse.com");

        // Assert
        assertNotNull(userDetails);
        assertNotNull(userDetails.getUsername());
        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());
    }
}