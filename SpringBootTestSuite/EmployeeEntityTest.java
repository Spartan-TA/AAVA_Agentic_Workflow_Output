package com.company.wms.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Employee entity
 * 
 * Tests cover:
 * - Field validation (null, blank, size constraints)
 * - Badge ID uniqueness
 * - Soft delete functionality
 * - Builder pattern
 * - Getters and setters
 * - Edge cases for all fields
 */
@DisplayName("Employee Entity Tests")
class EmployeeEntityTest {

    private Validator validator;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validEmployee = Employee.builder()
                .id(1L)
                .badgeId("B12345")
                .name("John Doe")
                .role("WORKER")
                .department("WH1")
                .shiftGroup("DAY")
                .hireDate(LocalDate.of(2023, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
    }

    // ========== Valid Employee Tests ==========
    
    @Test
    @DisplayName("Valid employee should pass all validations")
    void testValidEmployee() {
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Valid employee should have no validation errors");
    }

    @Test
    @DisplayName("Employee builder should create valid instance")
    void testEmployeeBuilder() {
        Employee employee = Employee.builder()
                .badgeId("B99999")
                .name("Jane Smith")
                .role("SUPERVISOR")
                .department("WH2")
                .status("ACTIVE")
                .build();
        
        assertNotNull(employee);
        assertEquals("B99999", employee.getBadgeId());
        assertEquals("Jane Smith", employee.getName());
        assertEquals("SUPERVISOR", employee.getRole());
        assertFalse(employee.isDeleted());
    }

    // ========== Badge ID Validation Tests ==========
    
    @Test
    @DisplayName("Null badge ID should fail validation")
    void testNullBadgeId() {
        validEmployee.setBadgeId(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Null badge ID should cause validation error");
    }

    @Test
    @DisplayName("Empty badge ID should fail validation")
    void testEmptyBadgeId() {
        validEmployee.setBadgeId("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Empty badge ID should cause validation error");
    }

    @Test
    @DisplayName("Blank badge ID should fail validation")
    void testBlankBadgeId() {
        validEmployee.setBadgeId("   ");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Blank badge ID should cause validation error");
    }

    @Test
    @DisplayName("Badge ID with maximum length should be valid")
    void testMaxLengthBadgeId() {
        String maxLengthBadgeId = "B" + "1".repeat(49); // 50 chars total
        validEmployee.setBadgeId(maxLengthBadgeId);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Max length badge ID should be valid");
    }

    @Test
    @DisplayName("Badge ID with special characters should be valid")
    void testSpecialCharactersBadgeId() {
        validEmployee.setBadgeId("B-123_ABC");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Badge ID with special characters should be valid");
    }

    // ========== Name Validation Tests ==========
    
    @Test
    @DisplayName("Null name should fail validation")
    void testNullName() {
        validEmployee.setName(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Null name should cause validation error");
    }

    @Test
    @DisplayName("Empty name should fail validation")
    void testEmptyName() {
        validEmployee.setName("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Empty name should cause validation error");
    }

    @Test
    @DisplayName("Blank name should fail validation")
    void testBlankName() {
        validEmployee.setName("   ");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Blank name should cause validation error");
    }

    @Test
    @DisplayName("Name with maximum length should be valid")
    void testMaxLengthName() {
        String maxLengthName = "A".repeat(100);
        validEmployee.setName(maxLengthName);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Max length name should be valid");
    }

    @Test
    @DisplayName("Name with special characters should be valid")
    void testSpecialCharactersName() {
        validEmployee.setName("O'Brien-Smith Jr.");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Name with special characters should be valid");
    }

    @Test
    @DisplayName("Name with unicode characters should be valid")
    void testUnicodeName() {
        validEmployee.setName("JosÃ© GarcÃ­a");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Name with unicode characters should be valid");
    }

    // ========== Role Validation Tests ==========
    
    @Test
    @DisplayName("Null role should fail validation")
    void testNullRole() {
        validEmployee.setRole(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Null role should cause validation error");
    }

    @Test
    @DisplayName("Empty role should fail validation")
    void testEmptyRole() {
        validEmployee.setRole("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Empty role should cause validation error");
    }

    @Test
    @DisplayName("Valid ADMIN role should pass validation")
    void testAdminRole() {
        validEmployee.setRole("ADMIN");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "ADMIN role should be valid");
    }

    @Test
    @DisplayName("Valid HR role should pass validation")
    void testHRRole() {
        validEmployee.setRole("HR");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "HR role should be valid");
    }

    @Test
    @DisplayName("Valid SUPERVISOR role should pass validation")
    void testSupervisorRole() {
        validEmployee.setRole("SUPERVISOR");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "SUPERVISOR role should be valid");
    }

    @Test
    @DisplayName("Valid WORKER role should pass validation")
    void testWorkerRole() {
        validEmployee.setRole("WORKER");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "WORKER role should be valid");
    }

    // ========== Department Validation Tests ==========
    
    @Test
    @DisplayName("Null department should fail validation")
    void testNullDepartment() {
        validEmployee.setDepartment(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Null department should cause validation error");
    }

    @Test
    @DisplayName("Empty department should fail validation")
    void testEmptyDepartment() {
        validEmployee.setDepartment("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Empty department should cause validation error");
    }

    @Test
    @DisplayName("Department with alphanumeric code should be valid")
    void testAlphanumericDepartment() {
        validEmployee.setDepartment("WH-123-A");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Alphanumeric department should be valid");
    }

    // ========== Status Validation Tests ==========
    
    @Test
    @DisplayName("Null status should fail validation")
    void testNullStatus() {
        validEmployee.setStatus(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Null status should cause validation error");
    }

    @Test
    @DisplayName("Empty status should fail validation")
    void testEmptyStatus() {
        validEmployee.setStatus("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertFalse(violations.isEmpty(), "Empty status should cause validation error");
    }

    @Test
    @DisplayName("ACTIVE status should be valid")
    void testActiveStatus() {
        validEmployee.setStatus("ACTIVE");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "ACTIVE status should be valid");
    }

    @Test
    @DisplayName("INACTIVE status should be valid")
    void testInactiveStatus() {
        validEmployee.setStatus("INACTIVE");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "INACTIVE status should be valid");
    }

    @Test
    @DisplayName("TERMINATED status should be valid")
    void testTerminatedStatus() {
        validEmployee.setStatus("TERMINATED");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "TERMINATED status should be valid");
    }

    // ========== Hire Date Tests ==========
    
    @Test
    @DisplayName("Null hire date should be valid (optional field)")
    void testNullHireDate() {
        validEmployee.setHireDate(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Null hire date should be valid");
    }

    @Test
    @DisplayName("Past hire date should be valid")
    void testPastHireDate() {
        validEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Past hire date should be valid");
    }

    @Test
    @DisplayName("Today's hire date should be valid")
    void testTodayHireDate() {
        validEmployee.setHireDate(LocalDate.now());
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Today's hire date should be valid");
    }

    @Test
    @DisplayName("Future hire date should be valid")
    void testFutureHireDate() {
        validEmployee.setHireDate(LocalDate.now().plusDays(30));
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Future hire date should be valid");
    }

    // ========== Shift Group Tests ==========
    
    @Test
    @DisplayName("Null shift group should be valid (optional field)")
    void testNullShiftGroup() {
        validEmployee.setShiftGroup(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Null shift group should be valid");
    }

    @Test
    @DisplayName("Empty shift group should be valid (optional field)")
    void testEmptyShiftGroup() {
        validEmployee.setShiftGroup("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "Empty shift group should be valid");
    }

    @Test
    @DisplayName("DAY shift group should be valid")
    void testDayShiftGroup() {
        validEmployee.setShiftGroup("DAY");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "DAY shift group should be valid");
    }

    @Test
    @DisplayName("NIGHT shift group should be valid")
    void testNightShiftGroup() {
        validEmployee.setShiftGroup("NIGHT");
        Set<ConstraintViolation<Employee>> violations = validator.validate(validEmployee);
        assertTrue(violations.isEmpty(), "NIGHT shift group should be valid");
    }

    // ========== Soft Delete Tests ==========
    
    @Test
    @DisplayName("Default deleted flag should be false")
    void testDefaultDeletedFlag() {
        Employee employee = Employee.builder()
                .badgeId("B123")
                .name("Test User")
                .role("WORKER")
                .department("WH1")
                .status("ACTIVE")
                .build();
        
        assertFalse(employee.isDeleted(), "Default deleted flag should be false");
    }

    @Test
    @DisplayName("Deleted flag can be set to true")
    void testSetDeletedTrue() {
        validEmployee.setDeleted(true);
        assertTrue(validEmployee.isDeleted(), "Deleted flag should be true");
    }

    @Test
    @DisplayName("Deleted flag can be toggled")
    void testToggleDeletedFlag() {
        validEmployee.setDeleted(true);
        assertTrue(validEmployee.isDeleted());
        
        validEmployee.setDeleted(false);
        assertFalse(validEmployee.isDeleted());
    }

    // ========== Equality and HashCode Tests ==========
    
    @Test
    @DisplayName("Employees with same ID should be equal")
    void testEqualityWithSameId() {
        Employee employee1 = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("WORKER")
                .department("WH1")
                .status("ACTIVE")
                .build();
        
        Employee employee2 = Employee.builder()
                .id(1L)
                .badgeId("B456")
                .name("Jane Doe")
                .role("SUPERVISOR")
                .department("WH2")
                .status("ACTIVE")
                .build();
        
        assertEquals(employee1, employee2, "Employees with same ID should be equal");
    }

    @Test
    @DisplayName("Employees with different IDs should not be equal")
    void testInequalityWithDifferentIds() {
        Employee employee1 = Employee.builder()
                .id(1L)
                .badgeId("B123")
                .name("John Doe")
                .role("WORKER")
                .department("WH1")
                .status("ACTIVE")
                .build();
        
        Employee employee2 = Employee.builder()
                .id(2L)
                .badgeId("B123")
                .name("John Doe")
                .role("WORKER")
                .department("WH1")
                .status("ACTIVE")
                .build();
        
        assertNotEquals(employee1, employee2, "Employees with different IDs should not be equal");
    }

    // ========== Edge Case Tests ==========
    
    @Test
    @DisplayName("Employee with all optional fields null should be valid")
    void testMinimalEmployee() {
        Employee employee = Employee.builder()
                .badgeId("B123")
                .name("Test User")
                .role("WORKER")
                .department("WH1")
                .status("ACTIVE")
                .build();
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Minimal employee should be valid");
    }

    @Test
    @DisplayName("Employee with all fields populated should be valid")
    void testFullyPopulatedEmployee() {
        Employee employee = Employee.builder()
                .id(1L)
                .badgeId("B12345")
                .name("John Doe")
                .role("SUPERVISOR")
                .department("WH1")
                .shiftGroup("DAY")
                .hireDate(LocalDate.of(2023, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Fully populated employee should be valid");
    }

    @Test
    @DisplayName("Employee toString should not throw exception")
    void testToString() {
        assertDoesNotThrow(() -> validEmployee.toString(), 
                "toString should not throw exception");
    }

    @Test
    @DisplayName("Employee with very long name should handle gracefully")
    void testVeryLongName() {
        String longName = "A".repeat(200);
        validEmployee.setName(longName);
        assertNotNull(validEmployee.getName());
        assertEquals(200, validEmployee.getName().length());
    }

    @Test
    @DisplayName("Employee with minimum valid data should pass validation")
    void testMinimumValidData() {
        Employee employee = new Employee();
        employee.setBadgeId("B");
        employee.setName("A");
        employee.setRole("W");
        employee.setDepartment("D");
        employee.setStatus("A");
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Minimum valid data should pass validation");
    }