package com.warehouse.employee.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit Test Suite for Employee Entity
 * 
 * Tests cover:
 * - Normal field assignments and getters/setters
 * - Validation constraints (NotBlank, NotNull)
 * - Edge cases (null values, empty strings, boundary dates)
 * - Lifecycle callbacks (@PrePersist, @PreUpdate)
 * - Soft delete functionality
 * - Timestamp management
 */
@DisplayName("Employee Entity Test Suite")
public class EmployeeEntityTest {

    private Validator validator;
    private Employee employee;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test valid employee creation with all required fields")
    void testValidEmployeeCreation() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP001");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.setDepartment("Warehouse");
        employee.setShiftGroup("Morning");
        employee.setHireDate(LocalDate.of(2024, 1, 15));

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Valid employee should have no validation errors");
        assertEquals("John Doe", employee.getName());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("WORKER", employee.getRole());
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test employee with all fields populated")
    void testEmployeeWithAllFields() {
        // Arrange & Act
        employee.setId(1L);
        employee.setName("Jane Smith");
        employee.setBadgeId("EMP002");
        employee.setRole("SUPERVISOR");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("Evening");
        employee.setHireDate(LocalDate.of(2023, 6, 1));
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        // Assert
        assertNotNull(employee.getId());
        assertEquals("Jane Smith", employee.getName());
        assertEquals("EMP002", employee.getBadgeId());
        assertEquals("SUPERVISOR", employee.getRole());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("Evening", employee.getShiftGroup());
        assertNotNull(employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.getDeleted());
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Test name field cannot be null")
    void testNameCannotBeNull() {
        // Arrange
        employee.setName(null);
        employee.setBadgeId("EMP003");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Test name field cannot be empty string")
    void testNameCannotBeEmpty() {
        // Arrange
        employee.setName("");
        employee.setBadgeId("EMP004");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Test name field cannot be blank (whitespace only)")
    void testNameCannotBeBlank() {
        // Arrange
        employee.setName("   ");
        employee.setBadgeId("EMP005");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test badgeId field cannot be null")
    void testBadgeIdCannotBeNull() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId(null);
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test badgeId field cannot be empty")
    void testBadgeIdCannotBeEmpty() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test role field cannot be null")
    void testRoleCannotBeNull() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP006");
        employee.setRole(null);
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Test status field cannot be null")
    void testStatusCannotBeNull() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP007");
        employee.setRole("WORKER");
        employee.setStatus(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test employee with maximum length name (100 characters)")
    void testMaxLengthName() {
        // Arrange
        String maxName = "A".repeat(100);
        employee.setName(maxName);
        employee.setBadgeId("EMP008");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(100, employee.getName().length());
    }

    @Test
    @DisplayName("Test employee with maximum length badgeId (50 characters)")
    void testMaxLengthBadgeId() {
        // Arrange
        String maxBadgeId = "B".repeat(50);
        employee.setName("John Doe");
        employee.setBadgeId(maxBadgeId);
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(50, employee.getBadgeId().length());
    }

    @Test
    @DisplayName("Test employee with special characters in name")
    void testSpecialCharactersInName() {
        // Arrange
        employee.setName("O'Brien-Smith Jr.");
        employee.setBadgeId("EMP009");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("O'Brien-Smith Jr.", employee.getName());
    }

    @Test
    @DisplayName("Test employee with alphanumeric badgeId")
    void testAlphanumericBadgeId() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP-2024-001");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("EMP-2024-001", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test employee with past hire date")
    void testPastHireDate() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP010");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.setHireDate(LocalDate.of(2020, 1, 1));

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertTrue(employee.getHireDate().isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("Test employee with current date as hire date")
    void testCurrentDateHireDate() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP011");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.setHireDate(LocalDate.now());

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(LocalDate.now(), employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee with null optional fields")
    void testNullOptionalFields() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP012");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.setDepartment(null);
        employee.setShiftGroup(null);
        employee.setHireDate(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Optional fields can be null");
        assertNull(employee.getDepartment());
        assertNull(employee.getShiftGroup());
        assertNull(employee.getHireDate());
    }

    // ========== SOFT DELETE TESTS ==========

    @Test
    @DisplayName("Test employee soft delete flag defaults to false")
    void testSoftDeleteDefaultValue() {
        // Arrange & Act
        Employee newEmployee = new Employee();

        // Assert
        assertFalse(newEmployee.getDeleted(), "Deleted flag should default to false");
    }

    @Test
    @DisplayName("Test employee soft delete flag can be set to true")
    void testSoftDeleteSetToTrue() {
        // Arrange
        employee.setDeleted(true);

        // Act & Assert
        assertTrue(employee.getDeleted());
    }

    @Test
    @DisplayName("Test employee soft delete with status change")
    void testSoftDeleteWithStatusChange() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP013");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);

        // Act
        employee.setDeleted(true);
        employee.setStatus("INACTIVE");

        // Assert
        assertTrue(employee.getDeleted());
        assertEquals("INACTIVE", employee.getStatus());
    }

    // ========== ROLE VALIDATION TESTS ==========

    @Test
    @DisplayName("Test valid ADMIN role")
    void testValidAdminRole() {
        // Arrange
        employee.setName("Admin User");
        employee.setBadgeId("ADMIN001");
        employee.setRole("ADMIN");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("ADMIN", employee.getRole());
    }

    @Test
    @DisplayName("Test valid HR role")
    void testValidHRRole() {
        // Arrange
        employee.setName("HR User");
        employee.setBadgeId("HR001");
        employee.setRole("HR");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("HR", employee.getRole());
    }

    @Test
    @DisplayName("Test valid SUPERVISOR role")
    void testValidSupervisorRole() {
        // Arrange
        employee.setName("Supervisor User");
        employee.setBadgeId("SUP001");
        employee.setRole("SUPERVISOR");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("SUPERVISOR", employee.getRole());
    }

    @Test
    @DisplayName("Test valid WORKER role")
    void testValidWorkerRole() {
        // Arrange
        employee.setName("Worker User");
        employee.setBadgeId("WRK001");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("WORKER", employee.getRole());
    }

    // ========== STATUS VALIDATION TESTS ==========

    @Test
    @DisplayName("Test valid ACTIVE status")
    void testValidActiveStatus() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP014");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test valid INACTIVE status")
    void testValidInactiveStatus() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP015");
        employee.setRole("WORKER");
        employee.setStatus("INACTIVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("INACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test valid ON_LEAVE status")
    void testValidOnLeaveStatus() {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP016");
        employee.setRole("WORKER");
        employee.setStatus("ON_LEAVE");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("ON_LEAVE", employee.getStatus());
    }

    // ========== TIMESTAMP TESTS ==========

    @Test
    @DisplayName("Test createdAt and updatedAt are set on creation")
    void testTimestampsOnCreation() {
        // Arrange
        Employee newEmployee = new Employee();
        newEmployee.setName("John Doe");
        newEmployee.setBadgeId("EMP017");
        newEmployee.setRole("WORKER");
        newEmployee.setStatus("ACTIVE");

        // Act
        newEmployee.onCreate();

        // Assert
        assertNotNull(newEmployee.getCreatedAt());
        assertNotNull(newEmployee.getUpdatedAt());
        assertEquals(newEmployee.getCreatedAt(), newEmployee.getUpdatedAt());
    }

    @Test
    @DisplayName("Test updatedAt is updated on modification")
    void testUpdatedAtOnModification() throws InterruptedException {
        // Arrange
        employee.setName("John Doe");
        employee.setBadgeId("EMP018");
        employee.setRole("WORKER");
        employee.setStatus("ACTIVE");
        employee.onCreate();
        LocalDateTime originalUpdatedAt = employee.getUpdatedAt();

        // Act
        Thread.sleep(10); // Small delay to ensure timestamp difference
        employee.onUpdate();

        // Assert
        assertNotNull(employee.getUpdatedAt());
        assertTrue(employee.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}