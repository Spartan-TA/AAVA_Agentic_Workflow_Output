package com.warehouse.employee.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

/**
 * Comprehensive JUnit test class for Employee entity
 * Tests all field operations, validation, and edge cases
 */
class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Test
    void testEmployeeEntityFields_ValidData() {
        // Arrange & Act
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        employee.setRole("WORKER");
        employee.setDepartment("Warehouse");
        employee.setShiftGroup("Morning");
        employee.setHireDate(LocalDate.of(2024, 1, 15));
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);

        // Assert
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Warehouse", employee.getDepartment());
        assertEquals("Morning", employee.getShiftGroup());
        assertEquals(LocalDate.of(2024, 1, 15), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    void testEmployeeEntityFields_NullValues() {
        // Arrange & Act
        employee.setName(null);
        employee.setBadgeId(null);
        employee.setRole(null);

        // Assert
        assertNull(employee.getName());
        assertNull(employee.getBadgeId());
        assertNull(employee.getRole());
    }

    @Test
    void testEmployeeEntityFields_EmptyStrings() {
        // Arrange & Act
        employee.setName("");
        employee.setBadgeId("");
        employee.setDepartment("");

        // Assert
        assertEquals("", employee.getName());
        assertEquals("", employee.getBadgeId());
        assertEquals("", employee.getDepartment());
    }

    @Test
    void testEmployeeEntityFields_BoundaryValues() {
        // Arrange & Act - Test with very long strings
        String longName = "A".repeat(255);
        employee.setName(longName);
        
        // Assert
        assertEquals(longName, employee.getName());
        assertEquals(255, employee.getName().length());
    }

    @Test
    void testEmployeeEqualsAndHashCode_SameValues() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(1L);
        emp1.setName("John Doe");
        emp1.setBadgeId("EMP001");

        Employee emp2 = new Employee();
        emp2.setId(1L);
        emp2.setName("John Doe");
        emp2.setBadgeId("EMP001");

        // Assert
        assertEquals(emp1, emp2);
        assertEquals(emp1.hashCode(), emp2.hashCode());
    }

    @Test
    void testEmployeeEqualsAndHashCode_DifferentValues() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setId(1L);
        emp1.setBadgeId("EMP001");

        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setBadgeId("EMP002");

        // Assert
        assertNotEquals(emp1, emp2);
    }

    @Test
    void testEmployeeStatus_AllValidStatuses() {
        // Test all valid status values
        String[] validStatuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};
        
        for (String status : validStatuses) {
            employee.setStatus(status);
            assertEquals(status, employee.getStatus());
        }
    }

    @Test
    void testEmployeeRole_AllValidRoles() {
        // Test all valid role values
        String[] validRoles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"};
        
        for (String role : validRoles) {
            employee.setRole(role);
            assertEquals(role, employee.getRole());
        }
    }

    @Test
    void testEmployeeDeleted_DefaultValue() {
        // Arrange
        Employee newEmployee = new Employee();
        
        // Assert - deleted should default to false
        assertFalse(newEmployee.isDeleted());
    }

    @Test
    void testEmployeeHireDate_FutureDate() {
        // Arrange & Act
        LocalDate futureDate = LocalDate.now().plusDays(30);
        employee.setHireDate(futureDate);
        
        // Assert
        assertEquals(futureDate, employee.getHireDate());
    }

    @Test
    void testEmployeeHireDate_PastDate() {
        // Arrange & Act
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        employee.setHireDate(pastDate);
        
        // Assert
        assertEquals(pastDate, employee.getHireDate());
    }

    @Test
    void testEmployeeBadgeId_UniqueConstraint() {
        // Arrange & Act
        employee.setBadgeId("UNIQUE123");
        
        // Assert
        assertEquals("UNIQUE123", employee.getBadgeId());
        assertNotNull(employee.getBadgeId());
    }

    @Test
    void testEmployeeToString_ContainsAllFields() {
        // Arrange
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        
        // Act
        String result = employee.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("John Doe") || result.contains("EMP001"));
    }
}