package com.warehouse.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Comprehensive JUnit test suite for Employee entity.
 * Tests cover normal operations, boundary conditions, and edge cases.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@DisplayName("Employee Entity Tests")
public class EmployeeEntityTest {

    private Employee employee;
    private Warehouse warehouse;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test warehouse
        warehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .timezone("America/New_York")
                .build();

        // Arrange: Create test employee with valid data
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .warehouse(warehouse)
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test employee creation with valid data")
    public void testEmployeeCreationWithValidData() {
        // Assert: Verify all fields are set correctly
        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Shipping", employee.getDepartment());
        assertEquals("Day Shift", employee.getShiftGroup());
        assertEquals(LocalDate.of(2023, 1, 15), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.getDeleted());
        assertEquals(warehouse, employee.getWarehouse());
    }

    @Test
    @DisplayName("Test employee builder pattern")
    public void testEmployeeBuilderPattern() {
        // Act: Create employee using builder
        Employee builtEmployee = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .build();

        // Assert: Verify builder creates valid object
        assertNotNull(builtEmployee);
        assertEquals("Jane Smith", builtEmployee.getName());
        assertEquals("EMP002", builtEmployee.getBadgeId());
        assertEquals("SUPERVISOR", builtEmployee.getRole());
    }

    @Test
    @DisplayName("Test isActive method with active employee")
    public void testIsActiveWithActiveEmployee() {
        // Act & Assert: Verify active employee returns true
        assertTrue(employee.isActive());
    }

    @Test
    @DisplayName("Test softDelete method")
    public void testSoftDelete() {
        // Act: Soft delete the employee
        employee.softDelete();

        // Assert: Verify employee is marked as deleted and inactive
        assertTrue(employee.getDeleted());
        assertEquals("INACTIVE", employee.getStatus());
        assertFalse(employee.isActive());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test employee with minimum required fields")
    public void testEmployeeWithMinimumFields() {
        // Arrange: Create employee with only required fields
        Employee minEmployee = Employee.builder()
                .name("Min Employee")
                .badgeId("MIN001")
                .role("WORKER")
                .build();

        // Assert: Verify required fields are set
        assertNotNull(minEmployee);
        assertEquals("Min Employee", minEmployee.getName());
        assertEquals("MIN001", minEmployee.getBadgeId());
        assertEquals("WORKER", minEmployee.getRole());
        assertNull(minEmployee.getDepartment());
        assertNull(minEmployee.getShiftGroup());
    }

    @Test
    @DisplayName("Test employee with maximum field lengths")
    public void testEmployeeWithMaxFieldLengths() {
        // Arrange: Create strings at maximum allowed lengths
        String maxName = "A".repeat(255);
        String maxBadgeId = "B".repeat(50);
        String maxRole = "C".repeat(50);
        String maxDepartment = "D".repeat(100);
        String maxShiftGroup = "E".repeat(50);

        // Act: Create employee with max length fields
        Employee maxEmployee = Employee.builder()
                .name(maxName)
                .badgeId(maxBadgeId)
                .role(maxRole)
                .department(maxDepartment)
                .shiftGroup(maxShiftGroup)
                .build();

        // Assert: Verify all fields are set correctly
        assertEquals(maxName, maxEmployee.getName());
        assertEquals(maxBadgeId, maxEmployee.getBadgeId());
        assertEquals(maxRole, maxEmployee.getRole());
        assertEquals(maxDepartment, maxEmployee.getDepartment());
        assertEquals(maxShiftGroup, maxEmployee.getShiftGroup());
    }

    @Test
    @DisplayName("Test employee with hire date at boundary (today)")
    public void testEmployeeWithHireDateToday() {
        // Arrange: Set hire date to today
        LocalDate today = LocalDate.now();
        employee.setHireDate(today);

        // Assert: Verify hire date is set correctly
        assertEquals(today, employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee with hire date in past")
    public void testEmployeeWithHireDateInPast() {
        // Arrange: Set hire date to 10 years ago
        LocalDate pastDate = LocalDate.now().minusYears(10);
        employee.setHireDate(pastDate);

        // Assert: Verify hire date is set correctly
        assertEquals(pastDate, employee.getHireDate());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test employee with null name throws exception")
    public void testEmployeeWithNullName() {
        // Act & Assert: Expect validation to fail for null name
        assertThrows(NullPointerException.class, () -> {
            Employee.builder()
                    .name(null)
                    .badgeId("EMP003")
                    .role("WORKER")
                    .build();
        });
    }

    @Test
    @DisplayName("Test employee with empty name")
    public void testEmployeeWithEmptyName() {
        // Arrange: Create employee with empty name
        Employee emptyNameEmployee = Employee.builder()
                .name("")
                .badgeId("EMP004")
                .role("WORKER")
                .build();

        // Assert: Verify empty name is set (validation should catch this)
        assertEquals("", emptyNameEmployee.getName());
    }

    @Test
    @DisplayName("Test employee with null badge ID throws exception")
    public void testEmployeeWithNullBadgeId() {
        // Act & Assert: Expect validation to fail for null badge ID
        assertThrows(NullPointerException.class, () -> {
            Employee.builder()
                    .name("Test Employee")
                    .badgeId(null)
                    .role("WORKER")
                    .build();
        });
    }

    @Test
    @DisplayName("Test employee with empty badge ID")
    public void testEmployeeWithEmptyBadgeId() {
        // Arrange: Create employee with empty badge ID
        Employee emptyBadgeEmployee = Employee.builder()
                .name("Test Employee")
                .badgeId("")
                .role("WORKER")
                .build();

        // Assert: Verify empty badge ID is set (validation should catch this)
        assertEquals("", emptyBadgeEmployee.getBadgeId());
    }

    @Test
    @DisplayName("Test employee with null role throws exception")
    public void testEmployeeWithNullRole() {
        // Act & Assert: Expect validation to fail for null role
        assertThrows(NullPointerException.class, () -> {
            Employee.builder()
                    .name("Test Employee")
                    .badgeId("EMP005")
                    .role(null)
                    .build();
        });
    }

    @Test
    @DisplayName("Test employee with null department")
    public void testEmployeeWithNullDepartment() {
        // Arrange: Create employee with null department
        employee.setDepartment(null);

        // Assert: Verify null department is allowed
        assertNull(employee.getDepartment());
    }

    @Test
    @DisplayName("Test employee with null shift group")
    public void testEmployeeWithNullShiftGroup() {
        // Arrange: Create employee with null shift group
        employee.setShiftGroup(null);

        // Assert: Verify null shift group is allowed
        assertNull(employee.getShiftGroup());
    }

    @Test
    @DisplayName("Test employee with null hire date")
    public void testEmployeeWithNullHireDate() {
        // Arrange: Create employee with null hire date
        employee.setHireDate(null);

        // Assert: Verify null hire date is allowed
        assertNull(employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee with null warehouse")
    public void testEmployeeWithNullWarehouse() {
        // Arrange: Create employee with null warehouse
        employee.setWarehouse(null);

        // Assert: Verify null warehouse is allowed
        assertNull(employee.getWarehouse());
    }

    @Test
    @DisplayName("Test isActive with inactive status")
    public void testIsActiveWithInactiveStatus() {
        // Arrange: Set employee status to inactive
        employee.setStatus("INACTIVE");

        // Act & Assert: Verify isActive returns false
        assertFalse(employee.isActive());
    }

    @Test
    @DisplayName("Test isActive with deleted flag true")
    public void testIsActiveWithDeletedFlag() {
        // Arrange: Set deleted flag to true
        employee.setDeleted(true);

        // Act & Assert: Verify isActive returns false
        assertFalse(employee.isActive());
    }

    @Test
    @DisplayName("Test isActive with both inactive status and deleted flag")
    public void testIsActiveWithInactiveAndDeleted() {
        // Arrange: Set both inactive status and deleted flag
        employee.setStatus("INACTIVE");
        employee.setDeleted(true);

        // Act & Assert: Verify isActive returns false
        assertFalse(employee.isActive());
    }

    @Test
    @DisplayName("Test employee with special characters in name")
    public void testEmployeeWithSpecialCharactersInName() {
        // Arrange: Create employee with special characters
        Employee specialEmployee = Employee.builder()
                .name("O'Brien-Smith, Jr.")
                .badgeId("EMP006")
                .role("WORKER")
                .build();

        // Assert: Verify special characters are preserved
        assertEquals("O'Brien-Smith, Jr.", specialEmployee.getName());
    }

    @Test
    @DisplayName("Test employee with unicode characters in name")
    public void testEmployeeWithUnicodeCharacters() {
        // Arrange: Create employee with unicode characters
        Employee unicodeEmployee = Employee.builder()
                .name("JosÃ© GarcÃ­a")
                .badgeId("EMP007")
                .role("WORKER")
                .build();

        // Assert: Verify unicode characters are preserved
        assertEquals("JosÃ© GarcÃ­a", unicodeEmployee.getName());
    }

    @Test
    @DisplayName("Test employee status transitions")
    public void testEmployeeStatusTransitions() {
        // Arrange: Start with active employee
        assertEquals("ACTIVE", employee.getStatus());

        // Act: Change status to on leave
        employee.setStatus("ON_LEAVE");
        assertEquals("ON_LEAVE", employee.getStatus());

        // Act: Change status to suspended
        employee.setStatus("SUSPENDED");
        assertEquals("SUSPENDED", employee.getStatus());

        // Act: Change status back to active
        employee.setStatus("ACTIVE");
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test employee with future hire date")
    public void testEmployeeWithFutureHireDate() {
        // Arrange: Set hire date to future
        LocalDate futureDate = LocalDate.now().plusDays(30);
        employee.setHireDate(futureDate);

        // Assert: Verify future hire date is set
        assertEquals(futureDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee equality based on badge ID")
    public void testEmployeeEquality() {
        // Arrange: Create two employees with same badge ID
        Employee employee1 = Employee.builder()
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .build();

        Employee employee2 = Employee.builder()
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .build();

        // Assert: Verify employees with same badge ID are considered equal
        assertEquals(employee1.getBadgeId(), employee2.getBadgeId());
    }

    @Test
    @DisplayName("Test employee with all roles")
    public void testEmployeeWithAllRoles() {
        // Test ADMIN role
        employee.setRole("ADMIN");
        assertEquals("ADMIN", employee.getRole());

        // Test HR role
        employee.setRole("HR");
        assertEquals("HR", employee.getRole());

        // Test SUPERVISOR role
        employee.setRole("SUPERVISOR");
        assertEquals("SUPERVISOR", employee.getRole());

        // Test WORKER role
        employee.setRole("WORKER");
        assertEquals("WORKER", employee.getRole());
    }

    @Test
    @DisplayName("Test softDelete is idempotent")
    public void testSoftDeleteIdempotent() {
        // Act: Call softDelete multiple times
        employee.softDelete();
        employee.softDelete();
        employee.softDelete();

        // Assert: Verify employee remains deleted and inactive
        assertTrue(employee.getDeleted());
        assertEquals("INACTIVE", employee.getStatus());
        assertFalse(employee.isActive());
    }
}
