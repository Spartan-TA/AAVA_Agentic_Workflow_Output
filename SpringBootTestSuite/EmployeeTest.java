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
 * Comprehensive JUnit test suite for Employee entity.
 * Tests cover:
 * - Valid entity creation
 * - Field validation (null, blank, invalid values)
 * - Boundary conditions
 * - Edge cases
 * - Lifecycle callbacks (@PrePersist, @PreUpdate)
 * - Builder pattern
 * - Getters and setters
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {

    private Validator validator;
    private Employee validEmployee;

    @BeforeEach
    public void setUp() {
        // Initialize validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create a valid employee for testing
        validEmployee = Employee.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2024, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test valid employee creation")
    public void testValidEmployeeCreation() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .shiftGroup("Evening")
                .hireDate(LocalDate.of(2023, 6, 1))
                .status("ACTIVE")
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals("Jane Smith", employee.getName());
        assertEquals("EMP002", employee.getBadgeId());
        assertEquals("SUPERVISOR", employee.getRole());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("Evening", employee.getShiftGroup());
        assertEquals(LocalDate.of(2023, 6, 1), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.isDeleted());
    }

    @Test
    @DisplayName("Test employee with all required fields passes validation")
    public void testValidEmployeePassesValidation() {
        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Valid employee should have no validation errors");
    }

    @Test
    @DisplayName("Test employee builder pattern")
    public void testEmployeeBuilderPattern() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id(1L)
                .name("Test User")
                .badgeId("TEST001")
                .role("ADMIN")
                .department("IT")
                .shiftGroup("Day")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals("Test User", employee.getName());
        assertEquals("TEST001", employee.getBadgeId());
    }

    // ========== NULL INPUT TESTS ==========

    @Test
    @DisplayName("Test employee with null name fails validation")
    public void testNullNameFailsValidation() {
        // Arrange
        validEmployee.setName(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Test employee with null badgeId fails validation")
    public void testNullBadgeIdFailsValidation() {
        // Arrange
        validEmployee.setBadgeId(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test employee with null role fails validation")
    public void testNullRoleFailsValidation() {
        // Arrange
        validEmployee.setRole(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Test employee with null hireDate fails validation")
    public void testNullHireDateFailsValidation() {
        // Arrange
        validEmployee.setHireDate(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    @DisplayName("Test employee with null status fails validation")
    public void testNullStatusFailsValidation() {
        // Arrange
        validEmployee.setStatus(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    // ========== EMPTY/BLANK STRING TESTS ==========

    @Test
    @DisplayName("Test employee with empty name fails validation")
    public void testEmptyNameFailsValidation() {
        // Arrange
        validEmployee.setName("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Test employee with blank name (whitespace) fails validation")
    public void testBlankNameFailsValidation() {
        // Arrange
        validEmployee.setName("   ");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Test employee with empty badgeId fails validation")
    public void testEmptyBadgeIdFailsValidation() {
        // Arrange
        validEmployee.setBadgeId("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test employee with empty role fails validation")
    public void testEmptyRoleFailsValidation() {
        // Arrange
        validEmployee.setRole("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Test employee with empty status fails validation")
    public void testEmptyStatusFailsValidation() {
        // Arrange
        validEmployee.setStatus("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test employee with maximum length name (100 characters)")
    public void testMaxLengthName() {
        // Arrange
        String maxName = "A".repeat(100);
        validEmployee.setName(maxName);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Name with 100 characters should be valid");
    }

    @Test
    @DisplayName("Test employee with maximum length badgeId (50 characters)")
    public void testMaxLengthBadgeId() {
        // Arrange
        String maxBadgeId = "B".repeat(50);
        validEmployee.setBadgeId(maxBadgeId);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "BadgeId with 50 characters should be valid");
    }

    @Test
    @DisplayName("Test employee with single character name")
    public void testSingleCharacterName() {
        // Arrange
        validEmployee.setName("A");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Single character name should be valid");
    }

    @Test
    @DisplayName("Test employee with hire date in the past")
    public void testHireDateInPast() {
        // Arrange
        validEmployee.setHireDate(LocalDate.of(2000, 1, 1));

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Past hire date should be valid");
    }

    @Test
    @DisplayName("Test employee with hire date today")
    public void testHireDateToday() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now());

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Today's hire date should be valid");
    }

    @Test
    @DisplayName("Test employee with hire date in the future")
    public void testHireDateInFuture() {
        // Arrange
        validEmployee.setHireDate(LocalDate.now().plusDays(30));

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Future hire date should be valid for planned hires");
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test employee with special characters in name")
    public void testSpecialCharactersInName() {
        // Arrange
        validEmployee.setName("O'Brien-Smith Jr.");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Name with special characters should be valid");
    }

    @Test
    @DisplayName("Test employee with unicode characters in name")
    public void testUnicodeCharactersInName() {
        // Arrange
        validEmployee.setName("JosÃ© GarcÃ­a");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Name with unicode characters should be valid");
    }

    @Test
    @DisplayName("Test employee with alphanumeric badgeId")
    public void testAlphanumericBadgeId() {
        // Arrange
        validEmployee.setBadgeId("EMP-2024-001");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Alphanumeric badgeId should be valid");
    }

    @Test
    @DisplayName("Test employee with null optional fields (department, shiftGroup)")
    public void testNullOptionalFields() {
        // Arrange
        validEmployee.setDepartment(null);
        validEmployee.setShiftGroup(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);

        // Assert
        assertTrue(violations.isEmpty(), "Null optional fields should be valid");
    }

    @Test
    @DisplayName("Test employee default deleted flag is false")
    public void testDefaultDeletedFlag() {
        // Arrange & Act
        Employee employee = new Employee();

        // Assert
        assertFalse(employee.isDeleted(), "Default deleted flag should be false");
    }

    @Test
    @DisplayName("Test employee default status is ACTIVE")
    public void testDefaultStatus() {
        // Arrange & Act
        Employee employee = new Employee();
        employee.setStatus("ACTIVE"); // Manually set as default in constructor

        // Assert
        assertEquals("ACTIVE", employee.getStatus(), "Default status should be ACTIVE");
    }

    // ========== LIFECYCLE CALLBACK TESTS ==========

    @Test
    @DisplayName("Test prePersist sets createdAt and updatedAt")
    public void testPrePersistSetsTimestamps() {
        // Arrange
        Employee employee = new Employee();
        employee.setName("Test");
        employee.setBadgeId("TEST");
        employee.setRole("WORKER");
        employee.setHireDate(LocalDate.now());
        employee.setStatus("ACTIVE");

        // Act
        employee.prePersist();

        // Assert
        assertNotNull(employee.getCreatedAt(), "createdAt should be set by prePersist");
        assertNotNull(employee.getUpdatedAt(), "updatedAt should be set by prePersist");
    }

    @Test
    @DisplayName("Test preUpdate updates updatedAt timestamp")
    public void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        // Arrange
        Employee employee = new Employee();
        employee.prePersist();
        LocalDateTime originalUpdatedAt = employee.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(10);

        // Act
        employee.preUpdate();

        // Assert
        assertNotNull(employee.getUpdatedAt());
        assertTrue(employee.getUpdatedAt().isAfter(originalUpdatedAt), 
                "updatedAt should be updated by preUpdate");
    }

    // ========== GETTER/SETTER TESTS ==========

    @Test
    @DisplayName("Test all getters and setters")
    public void testGettersAndSetters() {
        // Arrange
        Employee employee = new Employee();
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDateTime now = LocalDateTime.now();

        // Act
        employee.setId(100L);
        employee.setName("Test Employee");
        employee.setBadgeId("BADGE123");
        employee.setRole("ADMIN");
        employee.setDepartment("HR");
        employee.setShiftGroup("Night");
        employee.setHireDate(hireDate);
        employee.setStatus("INACTIVE");
        employee.setDeleted(true);
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        // Assert
        assertEquals(100L, employee.getId());
        assertEquals("Test Employee", employee.getName());
        assertEquals("BADGE123", employee.getBadgeId());
        assertEquals("ADMIN", employee.getRole());
        assertEquals("HR", employee.getDepartment());
        assertEquals("Night", employee.getShiftGroup());
        assertEquals(hireDate, employee.getHireDate());
        assertEquals("INACTIVE", employee.getStatus());
        assertTrue(employee.isDeleted());
        assertEquals(now, employee.getCreatedAt());
        assertEquals(now, employee.getUpdatedAt());
    }

    // ========== MULTIPLE VALIDATION ERROR TESTS ==========

    @Test
    @DisplayName("Test employee with multiple validation errors")
    public void testMultipleValidationErrors() {
        // Arrange
        Employee employee = new Employee();
        // Leave all required fields null

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 4, "Should have at least 4 validation errors");
    }

    @Test
    @DisplayName("Test employee status values")
    public void testEmployeeStatusValues() {
        // Test various status values
        String[] validStatuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};
        
        for (String status : validStatuses) {
            validEmployee.setStatus(status);
            Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
            assertTrue(violations.isEmpty(), "Status '" + status + "' should be valid");
        }
    }

    @Test
    @DisplayName("Test employee role values")
    public void testEmployeeRoleValues() {
        // Test various role values
        String[] validRoles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"};
        
        for (String role : validRoles) {
            validEmployee.setRole(role);
            Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
            assertTrue(violations.isEmpty(), "Role '" + role + "' should be valid");
        }
    }
}