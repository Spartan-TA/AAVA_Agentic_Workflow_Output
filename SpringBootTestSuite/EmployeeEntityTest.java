package com.warehouse.employee.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for Employee entity.
 * Tests validation constraints, field assignments, and edge cases.
 */
public class EmployeeEntityTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== POSITIVE TEST CASES ==========

    @Test
    public void testCreateEmployee_WithValidData_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("Warehouse", employee.getDepartment());
        assertEquals("Morning", employee.getShiftGroup());
        assertEquals(LocalDate.of(2023, 1, 15), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    public void testCreateEmployee_WithMinimalRequiredFields_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals("Jane Smith", employee.getName());
        assertEquals("EMP002", employee.getBadgeId());
        assertNull(employee.getShiftGroup());
        assertNull(employee.getHireDate());
    }

    @Test
    public void testCreateEmployee_WithCurrentDate_Success() {
        // Arrange & Act
        LocalDate today = LocalDate.now();
        Employee employee = Employee.builder()
                .name("Current Employee")
                .badgeId("EMP003")
                .role("ADMIN")
                .department("Management")
                .hireDate(today)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Current date should be valid for hireDate");
    }

    @Test
    public void testCreateEmployee_WithMaxLengthFields_Success() {
        // Arrange - Create strings at maximum allowed length
        String maxName = "A".repeat(100);
        String maxBadgeId = "B".repeat(20);
        String maxRole = "C".repeat(50);
        String maxDepartment = "D".repeat(50);
        String maxShiftGroup = "E".repeat(50);
        String maxStatus = "F".repeat(20);

        // Act
        Employee employee = Employee.builder()
                .name(maxName)
                .badgeId(maxBadgeId)
                .role(maxRole)
                .department(maxDepartment)
                .shiftGroup(maxShiftGroup)
                .status(maxStatus)
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Maximum length fields should be valid");
    }

    // ========== VALIDATION CONSTRAINT TESTS ==========

    @Test
    public void testCreateEmployee_WithNullName_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name(null)
                .badgeId("EMP004")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Name cannot be null");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    public void testCreateEmployee_WithEmptyName_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("")
                .badgeId("EMP005")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Name cannot be empty");
    }

    @Test
    public void testCreateEmployee_WithBlankName_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("   ")
                .badgeId("EMP006")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Name cannot be blank");
    }

    @Test
    public void testCreateEmployee_WithNullBadgeId_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId(null)
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "BadgeId cannot be null");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    public void testCreateEmployee_WithEmptyBadgeId_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "BadgeId cannot be empty");
    }

    @Test
    public void testCreateEmployee_WithNullRole_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP007")
                .role(null)
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Role cannot be null");
    }

    @Test
    public void testCreateEmployee_WithNullDepartment_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP008")
                .role("WORKER")
                .department(null)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Department cannot be null");
    }

    @Test
    public void testCreateEmployee_WithNullStatus_ValidationFails() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP009")
                .role("WORKER")
                .department("Warehouse")
                .status(null)
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Status cannot be null");
    }

    // ========== LENGTH CONSTRAINT TESTS ==========

    @Test
    public void testCreateEmployee_WithNameExceedingMaxLength_ValidationFails() {
        // Arrange - Create name exceeding 100 characters
        String longName = "A".repeat(101);

        // Act
        Employee employee = Employee.builder()
                .name(longName)
                .badgeId("EMP010")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Name exceeding 100 characters should fail validation");
    }

    @Test
    public void testCreateEmployee_WithBadgeIdExceedingMaxLength_ValidationFails() {
        // Arrange - Create badgeId exceeding 20 characters
        String longBadgeId = "B".repeat(21);

        // Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId(longBadgeId)
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "BadgeId exceeding 20 characters should fail validation");
    }

    @Test
    public void testCreateEmployee_WithRoleExceedingMaxLength_ValidationFails() {
        // Arrange - Create role exceeding 50 characters
        String longRole = "C".repeat(51);

        // Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP011")
                .role(longRole)
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Role exceeding 50 characters should fail validation");
    }

    // ========== DATE CONSTRAINT TESTS ==========

    @Test
    public void testCreateEmployee_WithFutureHireDate_ValidationFails() {
        // Arrange - Create future date
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP012")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(futureDate)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Future hire date should fail validation");
    }

    @Test
    public void testCreateEmployee_WithPastHireDate_Success() {
        // Arrange - Create past date
        LocalDate pastDate = LocalDate.now().minusYears(5);

        // Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP013")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(pastDate)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Past hire date should be valid");
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateEmployee_WithSingleCharacterFields_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("A")
                .badgeId("1")
                .role("W")
                .department("D")
                .status("A")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Single character fields should be valid");
    }

    @Test
    public void testCreateEmployee_WithSpecialCharactersInName_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("O'Brien-Smith Jr.")
                .badgeId("EMP014")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Special characters in name should be valid");
    }

    @Test
    public void testCreateEmployee_WithNumericBadgeId_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("123456")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Numeric badgeId should be valid");
    }

    @Test
    public void testCreateEmployee_WithAlphanumericBadgeId_Success() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP-2024-001")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Alphanumeric badgeId should be valid");
    }

    @Test
    public void testCreateEmployee_WithDifferentStatusValues_Success() {
        // Test ACTIVE status
        Employee activeEmployee = Employee.builder()
                .name("Active Employee")
                .badgeId("EMP015")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations1 = validator.validate(activeEmployee);
        assertTrue(violations1.isEmpty());

        // Test INACTIVE status
        Employee inactiveEmployee = Employee.builder()
                .name("Inactive Employee")
                .badgeId("EMP016")
                .role("WORKER")
                .department("Warehouse")
                .status("INACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations2 = validator.validate(inactiveEmployee);
        assertTrue(violations2.isEmpty());

        // Test DELETED status
        Employee deletedEmployee = Employee.builder()
                .name("Deleted Employee")
                .badgeId("EMP017")
                .role("WORKER")
                .department("Warehouse")
                .status("DELETED")
                .build();
        Set<ConstraintViolation<Employee>> violations3 = validator.validate(deletedEmployee);
        assertTrue(violations3.isEmpty());
    }

    @Test
    public void testCreateEmployee_WithNullShiftGroup_Success() {
        // Arrange & Act - shiftGroup is optional
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP018")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup(null)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Null shiftGroup should be valid as it's optional");
    }

    @Test
    public void testCreateEmployee_WithNullHireDate_Success() {
        // Arrange & Act - hireDate is optional
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP019")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(null)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Null hireDate should be valid as it's optional");
    }

    // ========== BOUNDARY VALUE TESTS ==========

    @Test
    public void testCreateEmployee_WithExactly100CharacterName_Success() {
        // Arrange - Exactly 100 characters
        String exactName = "A".repeat(100);

        // Act
        Employee employee = Employee.builder()
                .name(exactName)
                .badgeId("EMP020")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Exactly 100 character name should be valid");
        assertEquals(100, employee.getName().length());
    }

    @Test
    public void testCreateEmployee_WithExactly20CharacterBadgeId_Success() {
        // Arrange - Exactly 20 characters
        String exactBadgeId = "B".repeat(20);

        // Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId(exactBadgeId)
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Exactly 20 character badgeId should be valid");
        assertEquals(20, employee.getBadgeId().length());
    }

    @Test
    public void testCreateEmployee_WithVeryOldHireDate_Success() {
        // Arrange - Very old date (50 years ago)
        LocalDate oldDate = LocalDate.now().minusYears(50);

        // Act
        Employee employee = Employee.builder()
                .name("Veteran Employee")
                .badgeId("EMP021")
                .role("SUPERVISOR")
                .department("Warehouse")
                .hireDate(oldDate)
                .status("ACTIVE")
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Very old hire date should be valid");
    }
}