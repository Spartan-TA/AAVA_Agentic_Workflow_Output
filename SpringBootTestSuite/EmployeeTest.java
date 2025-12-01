package com.warehouse.employee.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

/**
 * Comprehensive unit tests for Employee entity.
 * Tests cover all fields, builder pattern, getters/setters, and edge cases.
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {

    private Employee employee;
    private LocalDate testHireDate;

    @BeforeEach
    public void setUp() {
        testHireDate = LocalDate.of(2023, 1, 15);
        employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(testHireDate)
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== NORMAL CASES ==========

    @Test
    @DisplayName("Test Employee creation with valid data")
    public void testEmployeeCreation_WithValidData_Success() {
        // Assert
        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("BADGE001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Warehouse", employee.getDepartment());
        assertEquals("Morning", employee.getShiftGroup());
        assertEquals(testHireDate, employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    @DisplayName("Test Employee builder pattern")
    public void testEmployeeBuilder_WithAllFields_Success() {
        // Arrange & Act
        Employee builtEmployee = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("BADGE002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();

        // Assert
        assertNotNull(builtEmployee);
        assertEquals(2L, builtEmployee.getId());
        assertEquals("Jane Smith", builtEmployee.getName());
        assertEquals("BADGE002", builtEmployee.getBadgeId());
    }

    @Test
    @DisplayName("Test all getters return correct values")
    public void testGetters_WithValidEmployee_ReturnsCorrectValues() {
        // Assert
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("BADGE001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Warehouse", employee.getDepartment());
        assertEquals("Morning", employee.getShiftGroup());
        assertEquals(testHireDate, employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    @DisplayName("Test all setters update values correctly")
    public void testSetters_WithNewValues_UpdatesSuccessfully() {
        // Act
        employee.setId(10L);
        employee.setName("Updated Name");
        employee.setBadgeId("BADGE999");
        employee.setRole("ADMIN");
        employee.setDepartment("Management");
        employee.setShiftGroup("Night");
        LocalDate newDate = LocalDate.of(2024, 1, 1);
        employee.setHireDate(newDate);
        employee.setStatus("INACTIVE");
        employee.setDeleted(true);

        // Assert
        assertEquals(10L, employee.getId());
        assertEquals("Updated Name", employee.getName());
        assertEquals("BADGE999", employee.getBadgeId());
        assertEquals("ADMIN", employee.getRole());
        assertEquals("Management", employee.getDepartment());
        assertEquals("Night", employee.getShiftGroup());
        assertEquals(newDate, employee.getHireDate());
        assertEquals("INACTIVE", employee.getStatus());
        assertTrue(employee.isDeleted());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Test Employee with null name")
    public void testEmployee_WithNullName_AllowsNull() {
        // Act
        employee.setName(null);

        // Assert
        assertNull(employee.getName());
    }

    @Test
    @DisplayName("Test Employee with empty name")
    public void testEmployee_WithEmptyName_AllowsEmpty() {
        // Act
        employee.setName("");

        // Assert
        assertEquals("", employee.getName());
    }

    @Test
    @DisplayName("Test Employee with null badgeId")
    public void testEmployee_WithNullBadgeId_AllowsNull() {
        // Act
        employee.setBadgeId(null);

        // Assert
        assertNull(employee.getBadgeId());
    }

    @Test
    @DisplayName("Test Employee with empty badgeId")
    public void testEmployee_WithEmptyBadgeId_AllowsEmpty() {
        // Act
        employee.setBadgeId("");

        // Assert
        assertEquals("", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test Employee with very long name")
    public void testEmployee_WithVeryLongName_AcceptsLongString() {
        // Arrange
        String longName = "A".repeat(500);

        // Act
        employee.setName(longName);

        // Assert
        assertEquals(longName, employee.getName());
        assertEquals(500, employee.getName().length());
    }

    @Test
    @DisplayName("Test Employee with special characters in name")
    public void testEmployee_WithSpecialCharactersInName_AcceptsSpecialChars() {
        // Arrange
        String specialName = "John O'Brien-Smith Jr.";

        // Act
        employee.setName(specialName);

        // Assert
        assertEquals(specialName, employee.getName());
    }

    @Test
    @DisplayName("Test Employee with null role")
    public void testEmployee_WithNullRole_AllowsNull() {
        // Act
        employee.setRole(null);

        // Assert
        assertNull(employee.getRole());
    }

    @Test
    @DisplayName("Test Employee with invalid role value")
    public void testEmployee_WithInvalidRole_AcceptsAnyString() {
        // Act
        employee.setRole("INVALID_ROLE");

        // Assert
        assertEquals("INVALID_ROLE", employee.getRole());
    }

    @Test
    @DisplayName("Test Employee with null department")
    public void testEmployee_WithNullDepartment_AllowsNull() {
        // Act
        employee.setDepartment(null);

        // Assert
        assertNull(employee.getDepartment());
    }

    @Test
    @DisplayName("Test Employee with null shiftGroup")
    public void testEmployee_WithNullShiftGroup_AllowsNull() {
        // Act
        employee.setShiftGroup(null);

        // Assert
        assertNull(employee.getShiftGroup());
    }

    @Test
    @DisplayName("Test Employee with null hireDate")
    public void testEmployee_WithNullHireDate_AllowsNull() {
        // Act
        employee.setHireDate(null);

        // Assert
        assertNull(employee.getHireDate());
    }

    @Test
    @DisplayName("Test Employee with future hireDate")
    public void testEmployee_WithFutureHireDate_AcceptsFutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusYears(1);

        // Act
        employee.setHireDate(futureDate);

        // Assert
        assertEquals(futureDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test Employee with very old hireDate")
    public void testEmployee_WithVeryOldHireDate_AcceptsOldDate() {
        // Arrange
        LocalDate oldDate = LocalDate.of(1950, 1, 1);

        // Act
        employee.setHireDate(oldDate);

        // Assert
        assertEquals(oldDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test Employee with null status")
    public void testEmployee_WithNullStatus_AllowsNull() {
        // Act
        employee.setStatus(null);

        // Assert
        assertNull(employee.getStatus());
    }

    @Test
    @DisplayName("Test Employee deleted flag toggle")
    public void testEmployee_DeletedFlagToggle_UpdatesCorrectly() {
        // Act & Assert
        assertFalse(employee.isDeleted());
        
        employee.setDeleted(true);
        assertTrue(employee.isDeleted());
        
        employee.setDeleted(false);
        assertFalse(employee.isDeleted());
    }

    // ========== BOUNDARY CONDITIONS ==========

    @Test
    @DisplayName("Test Employee with minimum valid ID")
    public void testEmployee_WithMinimumId_AcceptsZero() {
        // Act
        employee.setId(0L);

        // Assert
        assertEquals(0L, employee.getId());
    }

    @Test
    @DisplayName("Test Employee with maximum Long ID")
    public void testEmployee_WithMaximumId_AcceptsMaxLong() {
        // Act
        employee.setId(Long.MAX_VALUE);

        // Assert
        assertEquals(Long.MAX_VALUE, employee.getId());
    }

    @Test
    @DisplayName("Test Employee with negative ID")
    public void testEmployee_WithNegativeId_AcceptsNegative() {
        // Act
        employee.setId(-1L);

        // Assert
        assertEquals(-1L, employee.getId());
    }

    @Test
    @DisplayName("Test Employee with single character name")
    public void testEmployee_WithSingleCharName_AcceptsSingleChar() {
        // Act
        employee.setName("A");

        // Assert
        assertEquals("A", employee.getName());
    }

    @Test
    @DisplayName("Test Employee with whitespace-only name")
    public void testEmployee_WithWhitespaceOnlyName_AcceptsWhitespace() {
        // Act
        employee.setName("   ");

        // Assert
        assertEquals("   ", employee.getName());
    }

    @Test
    @DisplayName("Test Employee with numeric badgeId")
    public void testEmployee_WithNumericBadgeId_AcceptsNumeric() {
        // Act
        employee.setBadgeId("123456");

        // Assert
        assertEquals("123456", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test Employee with alphanumeric badgeId")
    public void testEmployee_WithAlphanumericBadgeId_AcceptsAlphanumeric() {
        // Act
        employee.setBadgeId("ABC123XYZ");

        // Assert
        assertEquals("ABC123XYZ", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test Employee no-args constructor")
    public void testEmployee_NoArgsConstructor_CreatesEmptyObject() {
        // Act
        Employee emptyEmployee = new Employee();

        // Assert
        assertNotNull(emptyEmployee);
        assertNull(emptyEmployee.getId());
        assertNull(emptyEmployee.getName());
        assertNull(emptyEmployee.getBadgeId());
        assertFalse(emptyEmployee.isDeleted());
    }

    @Test
    @DisplayName("Test Employee all-args constructor")
    public void testEmployee_AllArgsConstructor_CreatesFullObject() {
        // Act
        Employee fullEmployee = new Employee(
                5L,
                "Test User",
                "BADGE123",
                "HR",
                "Human Resources",
                "Day",
                LocalDate.of(2023, 5, 10),
                "ACTIVE",
                false
        );

        // Assert
        assertNotNull(fullEmployee);
        assertEquals(5L, fullEmployee.getId());
        assertEquals("Test User", fullEmployee.getName());
        assertEquals("BADGE123", fullEmployee.getBadgeId());
        assertEquals("HR", fullEmployee.getRole());
        assertEquals("Human Resources", fullEmployee.getDepartment());
        assertEquals("Day", fullEmployee.getShiftGroup());
        assertEquals(LocalDate.of(2023, 5, 10), fullEmployee.getHireDate());
        assertEquals("ACTIVE", fullEmployee.getStatus());
        assertFalse(fullEmployee.isDeleted());
    }
}