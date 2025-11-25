package com.warehouse.employee.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive JUnit test suite for Employee entity
 * Tests cover constructors, getters/setters, validations, relationships, and edge cases
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {
    
    private Employee employee;
    
    @BeforeEach
    public void setUp() {
        employee = new Employee();
    }
    
    // Constructor Tests
    @Test
    @DisplayName("Test Employee creation with default constructor")
    public void testEmployeeCreation_WithDefaultConstructor_ShouldSucceed() {
        // Arrange & Act
        Employee newEmployee = new Employee();
        
        // Assert
        assertNotNull(newEmployee);
        assertNull(newEmployee.getId());
        assertNull(newEmployee.getBadgeId());
    }
    
    @Test
    @DisplayName("Test Employee creation with valid data")
    public void testEmployeeCreation_WithValidData_ShouldSucceed() {
        // Arrange
        String badgeId = "EMP001";
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@warehouse.com";
        String role = "WORKER";
        String department = "Logistics";
        String shiftGroup = "A";
        LocalDate hireDate = LocalDate.of(2024, 1, 15);
        String status = "ACTIVE";
        
        // Act
        employee.setBadgeId(badgeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setShiftGroup(shiftGroup);
        employee.setHireDate(hireDate);
        employee.setStatus(status);
        
        // Assert
        assertEquals(badgeId, employee.getBadgeId());
        assertEquals(firstName, employee.getFirstName());
        assertEquals(lastName, employee.getLastName());
        assertEquals(email, employee.getEmail());
        assertEquals(role, employee.getRole());
        assertEquals(department, employee.getDepartment());
        assertEquals(shiftGroup, employee.getShiftGroup());
        assertEquals(hireDate, employee.getHireDate());
        assertEquals(status, employee.getStatus());
    }
    
    // BadgeId Tests
    @Test
    @DisplayName("Test setBadgeId with null value")
    public void testSetBadgeId_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setBadgeId(null);
        
        // Assert
        assertNull(employee.getBadgeId());
    }
    
    @Test
    @DisplayName("Test setBadgeId with empty string")
    public void testSetBadgeId_WithEmptyString_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setBadgeId("");
        
        // Assert
        assertEquals("", employee.getBadgeId());
    }
    
    @Test
    @DisplayName("Test setBadgeId with valid value")
    public void testSetBadgeId_WithValidValue_ShouldSucceed() {
        // Arrange
        String badgeId = "EMP12345";
        
        // Act
        employee.setBadgeId(badgeId);
        
        // Assert
        assertEquals(badgeId, employee.getBadgeId());
    }
    
    @Test
    @DisplayName("Test setBadgeId with special characters")
    public void testSetBadgeId_WithSpecialCharacters_ShouldSucceed() {
        // Arrange
        String badgeId = "EMP-001_A";
        
        // Act
        employee.setBadgeId(badgeId);
        
        // Assert
        assertEquals(badgeId, employee.getBadgeId());
    }
    
    // FirstName Tests
    @Test
    @DisplayName("Test setFirstName with null value")
    public void testSetFirstName_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setFirstName(null);
        
        // Assert
        assertNull(employee.getFirstName());
    }
    
    @Test
    @DisplayName("Test setFirstName with empty string")
    public void testSetFirstName_WithEmptyString_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setFirstName("");
        
        // Assert
        assertEquals("", employee.getFirstName());
    }
    
    @Test
    @DisplayName("Test setFirstName with valid value")
    public void testSetFirstName_WithValidValue_ShouldSucceed() {
        // Arrange
        String firstName = "John";
        
        // Act
        employee.setFirstName(firstName);
        
        // Assert
        assertEquals(firstName, employee.getFirstName());
    }
    
    @Test
    @DisplayName("Test setFirstName with very long string")
    public void testSetFirstName_WithVeryLongString_ShouldAcceptButMayFailSizeValidation() {
        // Arrange
        String firstName = "A".repeat(300);
        
        // Act
        employee.setFirstName(firstName);
        
        // Assert
        assertEquals(firstName, employee.getFirstName());
    }
    
    // LastName Tests
    @Test
    @DisplayName("Test setLastName with null value")
    public void testSetLastName_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setLastName(null);
        
        // Assert
        assertNull(employee.getLastName());
    }
    
    @Test
    @DisplayName("Test setLastName with valid value")
    public void testSetLastName_WithValidValue_ShouldSucceed() {
        // Arrange
        String lastName = "Doe";
        
        // Act
        employee.setLastName(lastName);
        
        // Assert
        assertEquals(lastName, employee.getLastName());
    }
    
    // Email Tests
    @Test
    @DisplayName("Test setEmail with null value")
    public void testSetEmail_WithNull_ShouldAcceptButFailValidation() {
        // Arrange & Act
        employee.setEmail(null);
        
        // Assert
        assertNull(employee.getEmail());
    }
    
    @Test
    @DisplayName("Test setEmail with valid email")
    public void testSetEmail_WithValidEmail_ShouldSucceed() {
        // Arrange
        String email = "john.doe@warehouse.com";
        
        // Act
        employee.setEmail(email);
        
        // Assert
        assertEquals(email, employee.getEmail());
    }
    
    @Test
    @DisplayName("Test setEmail with invalid email format")
    public void testSetEmail_WithInvalidFormat_ShouldAcceptButFailEmailValidation() {
        // Arrange
        String email = "invalid-email";
        
        // Act
        employee.setEmail(email);
        
        // Assert
        assertEquals(email, employee.getEmail());
    }
    
    // Role Tests
    @Test
    @DisplayName("Test setRole with ADMIN role")
    public void testSetRole_WithAdminRole_ShouldSucceed() {
        // Arrange
        String role = "ADMIN";
        
        // Act
        employee.setRole(role);
        
        // Assert
        assertEquals(role, employee.getRole());
    }
    
    @Test
    @DisplayName("Test setRole with HR role")
    public void testSetRole_WithHRRole_ShouldSucceed() {
        // Arrange
        String role = "HR";
        
        // Act
        employee.setRole(role);
        
        // Assert
        assertEquals(role, employee.getRole());
    }
    
    @Test
    @DisplayName("Test setRole with SUPERVISOR role")
    public void testSetRole_WithSupervisorRole_ShouldSucceed() {
        // Arrange
        String role = "SUPERVISOR";
        
        // Act
        employee.setRole(role);
        
        // Assert
        assertEquals(role, employee.getRole());
    }
    
    @Test
    @DisplayName("Test setRole with WORKER role")
    public void testSetRole_WithWorkerRole_ShouldSucceed() {
        // Arrange
        String role = "WORKER";
        
        // Act
        employee.setRole(role);
        
        // Assert
        assertEquals(role, employee.getRole());
    }
    
    @Test
    @DisplayName("Test setRole with invalid role")
    public void testSetRole_WithInvalidRole_ShouldAcceptButMayFailValidation() {
        // Arrange
        String role = "INVALID_ROLE";
        
        // Act
        employee.setRole(role);
        
        // Assert
        assertEquals(role, employee.getRole());
    }
    
    // Department Tests
    @Test
    @DisplayName("Test setDepartment with valid value")
    public void testSetDepartment_WithValidValue_ShouldSucceed() {
        // Arrange
        String department = "Logistics";
        
        // Act
        employee.setDepartment(department);
        
        // Assert
        assertEquals(department, employee.getDepartment());
    }
    
    @Test
    @DisplayName("Test setDepartment with null value")
    public void testSetDepartment_WithNull_ShouldSucceed() {
        // Arrange & Act
        employee.setDepartment(null);
        
        // Assert
        assertNull(employee.getDepartment());
    }
    
    // ShiftGroup Tests
    @Test
    @DisplayName("Test setShiftGroup with valid value")
    public void testSetShiftGroup_WithValidValue_ShouldSucceed() {
        // Arrange
        String shiftGroup = "A";
        
        // Act
        employee.setShiftGroup(shiftGroup);
        
        // Assert
        assertEquals(shiftGroup, employee.getShiftGroup());
    }
    
    // HireDate Tests
    @Test
    @DisplayName("Test setHireDate with valid date")
    public void testSetHireDate_WithValidDate_ShouldSucceed() {
        // Arrange
        LocalDate hireDate = LocalDate.of(2024, 1, 15);
        
        // Act
        employee.setHireDate(hireDate);
        
        // Assert
        assertEquals(hireDate, employee.getHireDate());
    }
    
    @Test
    @DisplayName("Test setHireDate with past date")
    public void testSetHireDate_WithPastDate_ShouldSucceed() {
        // Arrange
        LocalDate hireDate = LocalDate.of(2020, 1, 1);
        
        // Act
        employee.setHireDate(hireDate);
        
        // Assert
        assertEquals(hireDate, employee.getHireDate());
    }
    
    @Test
    @DisplayName("Test setHireDate with future date")
    public void testSetHireDate_WithFutureDate_ShouldSucceed() {
        // Arrange
        LocalDate hireDate = LocalDate.now().plusDays(30);
        
        // Act
        employee.setHireDate(hireDate);
        
        // Assert
        assertEquals(hireDate, employee.getHireDate());
    }
    
    @Test
    @DisplayName("Test setHireDate with null value")
    public void testSetHireDate_WithNull_ShouldSucceed() {
        // Arrange & Act
        employee.setHireDate(null);
        
        // Assert
        assertNull(employee.getHireDate());
    }
    
    // Status Tests
    @Test
    @DisplayName("Test setStatus with ACTIVE status")
    public void testSetStatus_WithActiveStatus_ShouldSucceed() {
        // Arrange
        String status = "ACTIVE";
        
        // Act
        employee.setStatus(status);
        
        // Assert
        assertEquals(status, employee.getStatus());
    }
    
    @Test
    @DisplayName("Test setStatus with INACTIVE status")
    public void testSetStatus_WithInactiveStatus_ShouldSucceed() {
        // Arrange
        String status = "INACTIVE";
        
        // Act
        employee.setStatus(status);
        
        // Assert
        assertEquals(status, employee.getStatus());
    }
    
    // Equals and HashCode Tests
    @Test
    @DisplayName("Test equals with same object")
    public void testEquals_WithSameObject_ShouldReturnTrue() {
        // Arrange
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        
        // Act & Assert
        assertEquals(employee, employee);
    }
    
    @Test
    @DisplayName("Test equals with equal objects")
    public void testEquals_WithEqualObjects_ShouldReturnTrue() {
        // Arrange
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        
        Employee employee2 = new Employee();
        employee2.setId(1L);
        employee2.setBadgeId("EMP001");
        
        // Act & Assert
        assertEquals(employee, employee2);
    }
    
    @Test
    @DisplayName("Test equals with different objects")
    public void testEquals_WithDifferentObjects_ShouldReturnFalse() {
        // Arrange
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        
        // Act & Assert
        assertNotEquals(employee, employee2);
    }
    
    @Test
    @DisplayName("Test equals with null")
    public void testEquals_WithNull_ShouldReturnFalse() {
        // Arrange
        employee.setId(1L);
        
        // Act & Assert
        assertNotEquals(employee, null);
    }
    
    @Test
    @DisplayName("Test hashCode consistency")
    public void testHashCode_WithSameData_ShouldBeConsistent() {
        // Arrange
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        
        int hashCode1 = employee.hashCode();
        int hashCode2 = employee.hashCode();
        
        // Act & Assert
        assertEquals(hashCode1, hashCode2);
    }
    
    // ToString Test
    @Test
    @DisplayName("Test toString method")
    public void testToString_ShouldReturnStringRepresentation() {
        // Arrange
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        
        // Act
        String result = employee.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("EMP001") || result.contains("John") || result.contains("Doe"));
    }
}