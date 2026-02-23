package com.wms.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
 * Tests all fields, validation annotations, constructors, and business logic.
 * 
 * @author WMS Test Team
 * @version 1.0.0
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {

    private Validator validator;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid employee for testing
        employee = new Employee(
            "WH001",
            "John Doe",
            "john.doe@warehouse.com",
            "Logistics",
            "Warehouse Associate",
            LocalDate.of(2024, 1, 15),
            "ACTIVE"
        );
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Test default constructor creates empty employee")
    public void testDefaultConstructor_CreatesEmptyEmployee() {
        Employee emp = new Employee();
        assertNotNull(emp);
        assertNull(emp.getId());
        assertNull(emp.getBadgeId());
        assertNull(emp.getName());
    }

    @Test
    @DisplayName("Test parameterized constructor sets all fields correctly")
    public void testParameterizedConstructor_SetsAllFieldsCorrectly() {
        assertEquals("WH001", employee.getBadgeId());
        assertEquals("John Doe", employee.getName());
        assertEquals("john.doe@warehouse.com", employee.getEmail());
        assertEquals("Logistics", employee.getDepartment());
        assertEquals("Warehouse Associate", employee.getRole());
        assertEquals(LocalDate.of(2024, 1, 15), employee.getHireDate());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.getDeleted());
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
    }

    // ========== Validation Tests - Normal Cases ==========

    @Test
    @DisplayName("Test valid employee passes all validations")
    public void testValidEmployee_PassesAllValidations() {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Valid employee should have no validation errors");
    }

    @Test
    @DisplayName("Test employee with all required fields is valid")
    public void testEmployeeWithAllRequiredFields_IsValid() {
        employee.setId(1L);
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    // ========== Validation Tests - Null Values ==========

    @Test
    @DisplayName("Test null badge ID fails validation")
    public void testNullBadgeId_FailsValidation() {
        employee.setBadgeId(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Badge ID is required")));
    }

    @Test
    @DisplayName("Test null name fails validation")
    public void testNullName_FailsValidation() {
        employee.setName(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Name is required")));
    }

    @Test
    @DisplayName("Test null email fails validation")
    public void testNullEmail_FailsValidation() {
        employee.setEmail(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    @DisplayName("Test null department fails validation")
    public void testNullDepartment_FailsValidation() {
        employee.setDepartment(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test null role fails validation")
    public void testNullRole_FailsValidation() {
        employee.setRole(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test null hire date fails validation")
    public void testNullHireDate_FailsValidation() {
        employee.setHireDate(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Hire date is required")));
    }

    @Test
    @DisplayName("Test null status fails validation")
    public void testNullStatus_FailsValidation() {
        employee.setStatus(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    // ========== Validation Tests - Empty Strings ==========

    @Test
    @DisplayName("Test empty badge ID fails validation")
    public void testEmptyBadgeId_FailsValidation() {
        employee.setBadgeId("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test empty name fails validation")
    public void testEmptyName_FailsValidation() {
        employee.setName("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test whitespace-only badge ID fails validation")
    public void testWhitespaceBadgeId_FailsValidation() {
        employee.setBadgeId("   ");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    // ========== Validation Tests - Invalid Email ==========

    @Test
    @DisplayName("Test invalid email format fails validation")
    public void testInvalidEmailFormat_FailsValidation() {
        employee.setEmail("invalid-email");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }

    @Test
    @DisplayName("Test email without @ symbol fails validation")
    public void testEmailWithoutAtSymbol_FailsValidation() {
        employee.setEmail("johndoewarehouse.com");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test email without domain fails validation")
    public void testEmailWithoutDomain_FailsValidation() {
        employee.setEmail("john.doe@");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Test valid email formats pass validation")
    public void testValidEmailFormats_PassValidation() {
        String[] validEmails = {
            "john.doe@warehouse.com",
            "jane_smith@company.co.uk",
            "employee123@test-domain.org",
            "user+tag@example.com"
        };
        
        for (String email : validEmails) {
            employee.setEmail(email);
            Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("Test ID getter and setter")
    public void testIdGetterSetter() {
        employee.setId(100L);
        assertEquals(100L, employee.getId());
    }

    @Test
    @DisplayName("Test badge ID getter and setter")
    public void testBadgeIdGetterSetter() {
        employee.setBadgeId("WH999");
        assertEquals("WH999", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test name getter and setter")
    public void testNameGetterSetter() {
        employee.setName("Jane Smith");
        assertEquals("Jane Smith", employee.getName());
    }

    @Test
    @DisplayName("Test email getter and setter")
    public void testEmailGetterSetter() {
        employee.setEmail("jane.smith@warehouse.com");
        assertEquals("jane.smith@warehouse.com", employee.getEmail());
    }

    @Test
    @DisplayName("Test department getter and setter")
    public void testDepartmentGetterSetter() {
        employee.setDepartment("Operations");
        assertEquals("Operations", employee.getDepartment());
    }

    @Test
    @DisplayName("Test role getter and setter")
    public void testRoleGetterSetter() {
        employee.setRole("Senior Associate");
        assertEquals("Senior Associate", employee.getRole());
    }

    @Test
    @DisplayName("Test hire date getter and setter")
    public void testHireDateGetterSetter() {
        LocalDate newDate = LocalDate.of(2023, 6, 1);
        employee.setHireDate(newDate);
        assertEquals(newDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test status getter and setter")
    public void testStatusGetterSetter() {
        employee.setStatus("INACTIVE");
        assertEquals("INACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test deleted flag getter and setter")
    public void testDeletedGetterSetter() {
        employee.setDeleted(true);
        assertTrue(employee.getDeleted());
        
        employee.setDeleted(false);
        assertFalse(employee.getDeleted());
    }

    @Test
    @DisplayName("Test created at getter and setter")
    public void testCreatedAtGetterSetter() {
        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        assertEquals(now, employee.getCreatedAt());
    }

    @Test
    @DisplayName("Test updated at getter and setter")
    public void testUpdatedAtGetterSetter() {
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        assertEquals(now, employee.getUpdatedAt());
    }

    // ========== Edge Cases - Boundary Values ==========

    @Test
    @DisplayName("Test very long badge ID")
    public void testVeryLongBadgeId() {
        String longBadgeId = "WH" + "0".repeat(100);
        employee.setBadgeId(longBadgeId);
        assertEquals(longBadgeId, employee.getBadgeId());
    }

    @Test
    @DisplayName("Test very long name")
    public void testVeryLongName() {
        String longName = "A".repeat(200);
        employee.setName(longName);
        assertEquals(longName, employee.getName());
    }

    @Test
    @DisplayName("Test special characters in name")
    public void testSpecialCharactersInName() {
        employee.setName("O'Brien-Smith Jr.");
        assertEquals("O'Brien-Smith Jr.", employee.getName());
    }

    @Test
    @DisplayName("Test hire date in the past")
    public void testHireDateInPast() {
        LocalDate pastDate = LocalDate.of(2000, 1, 1);
        employee.setHireDate(pastDate);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test hire date today")
    public void testHireDateToday() {
        employee.setHireDate(LocalDate.now());
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    // ========== Status Values Tests ==========

    @Test
    @DisplayName("Test all valid status values")
    public void testAllValidStatusValues() {
        String[] validStatuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};
        
        for (String status : validStatuses) {
            employee.setStatus(status);
            Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
            assertTrue(violations.isEmpty(), "Status " + status + " should be valid");
        }
    }

    // ========== Soft Delete Tests ==========

    @Test
    @DisplayName("Test default deleted flag is false")
    public void testDefaultDeletedFlag_IsFalse() {
        Employee newEmployee = new Employee();
        assertNull(newEmployee.getDeleted());
        
        Employee constructedEmployee = new Employee(
            "WH002", "Test User", "test@test.com",
            "Test", "Tester", LocalDate.now(), "ACTIVE"
        );
        assertFalse(constructedEmployee.getDeleted());
    }

    @Test
    @DisplayName("Test soft delete sets deleted flag to true")
    public void testSoftDelete_SetsDeletedFlagTrue() {
        employee.setDeleted(true);
        assertTrue(employee.getDeleted());
    }

    // ========== ToString Tests ==========

    @Test
    @DisplayName("Test toString contains key fields")
    public void testToString_ContainsKeyFields() {
        employee.setId(1L);
        String toString = employee.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("badgeId='WH001'"));
        assertTrue(toString.contains("name='John Doe'"));
        assertTrue(toString.contains("email='john.doe@warehouse.com'"));
        assertTrue(toString.contains("department='Logistics'"));
        assertTrue(toString.contains("role='Warehouse Associate'"));
        assertTrue(toString.contains("status='ACTIVE'"));
    }

    @Test
    @DisplayName("Test toString with null ID")
    public void testToString_WithNullId() {
        String toString = employee.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("id=null"));
    }

    // ========== Timestamp Tests ==========

    @Test
    @DisplayName("Test created at and updated at are set on construction")
    public void testTimestamps_SetOnConstruction() {
        Employee newEmployee = new Employee(
            "WH003", "Test User", "test@test.com",
            "Test", "Tester", LocalDate.now(), "ACTIVE"
        );
        
        assertNotNull(newEmployee.getCreatedAt());
        assertNotNull(newEmployee.getUpdatedAt());
        assertEquals(newEmployee.getCreatedAt(), newEmployee.getUpdatedAt());
    }

    @Test
    @DisplayName("Test updated at can be modified independently")
    public void testUpdatedAt_CanBeModifiedIndependently() {
        LocalDateTime created = employee.getCreatedAt();
        LocalDateTime updated = LocalDateTime.now().plusDays(1);
        
        employee.setUpdatedAt(updated);
        
        assertEquals(created, employee.getCreatedAt());
        assertEquals(updated, employee.getUpdatedAt());
        assertNotEquals(created, updated);
    }

    // ========== Multiple Validation Errors ==========

    @Test
    @DisplayName("Test multiple validation errors are reported")
    public void testMultipleValidationErrors_AreReported() {
        employee.setBadgeId(null);
        employee.setName("");
        employee.setEmail("invalid");
        employee.setHireDate(null);
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.size() >= 4, "Should have at least 4 validation errors");
    }
}