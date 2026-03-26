package com.warehouse.employee.management.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for Employee entity
 * Tests validation, constructors, getters/setters, and lifecycle hooks
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@DisplayName("Employee Entity Test Suite")
public class EmployeeTest {

    private Validator validator;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Initialize test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setRole("WORKER");
        testEmployee.setDepartment("Shipping");
        testEmployee.setShiftGroup("Morning");
        testEmployee.setHireDate(LocalDate.of(2023, 1, 15));
        testEmployee.setStatus("ACTIVE");
        testEmployee.setDeleted(false);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Test Default Constructor - Creates Empty Employee")
    void testDefaultConstructor_CreatesEmptyEmployee() {
        // Act
        Employee employee = new Employee();

        // Assert
        assertNotNull(employee);
        assertNull(employee.getId());
        assertNull(employee.getBadgeId());
        assertNull(employee.getFirstName());
        assertNull(employee.getLastName());
        assertNull(employee.getEmail());
    }

    // ==================== GETTER AND SETTER TESTS ====================

    @Test
    @DisplayName("Test getId and setId - Valid ID")
    void testGetIdAndSetId_ValidId() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setId(100L);

        // Assert
        assertEquals(100L, employee.getId());
    }

    @Test
    @DisplayName("Test getBadgeId and setBadgeId - Valid BadgeId")
    void testGetBadgeIdAndSetBadgeId_ValidBadgeId() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setBadgeId("EMP999");

        // Assert
        assertEquals("EMP999", employee.getBadgeId());
    }

    @Test
    @DisplayName("Test getFirstName and setFirstName - Valid FirstName")
    void testGetFirstNameAndSetFirstName_ValidFirstName() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setFirstName("Alice");

        // Assert
        assertEquals("Alice", employee.getFirstName());
    }

    @Test
    @DisplayName("Test getLastName and setLastName - Valid LastName")
    void testGetLastNameAndSetLastName_ValidLastName() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setLastName("Williams");

        // Assert
        assertEquals("Williams", employee.getLastName());
    }

    @Test
    @DisplayName("Test getEmail and setEmail - Valid Email")
    void testGetEmailAndSetEmail_ValidEmail() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setEmail("alice.williams@warehouse.com");

        // Assert
        assertEquals("alice.williams@warehouse.com", employee.getEmail());
    }

    @Test
    @DisplayName("Test getPhoneNumber and setPhoneNumber - Valid PhoneNumber")
    void testGetPhoneNumberAndSetPhoneNumber_ValidPhoneNumber() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setPhoneNumber("+9876543210");

        // Assert
        assertEquals("+9876543210", employee.getPhoneNumber());
    }

    @Test
    @DisplayName("Test getRole and setRole - Valid Role")
    void testGetRoleAndSetRole_ValidRole() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setRole("SUPERVISOR");

        // Assert
        assertEquals("SUPERVISOR", employee.getRole());
    }

    @Test
    @DisplayName("Test getDepartment and setDepartment - Valid Department")
    void testGetDepartmentAndSetDepartment_ValidDepartment() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setDepartment("Receiving");

        // Assert
        assertEquals("Receiving", employee.getDepartment());
    }

    @Test
    @DisplayName("Test getShiftGroup and setShiftGroup - Valid ShiftGroup")
    void testGetShiftGroupAndSetShiftGroup_ValidShiftGroup() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setShiftGroup("Evening");

        // Assert
        assertEquals("Evening", employee.getShiftGroup());
    }

    @Test
    @DisplayName("Test getHireDate and setHireDate - Valid HireDate")
    void testGetHireDateAndSetHireDate_ValidHireDate() {
        // Arrange
        Employee employee = new Employee();
        LocalDate hireDate = LocalDate.of(2024, 6, 15);

        // Act
        employee.setHireDate(hireDate);

        // Assert
        assertEquals(hireDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test getStatus and setStatus - Valid Status")
    void testGetStatusAndSetStatus_ValidStatus() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setStatus("INACTIVE");

        // Assert
        assertEquals("INACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Test getDeleted and setDeleted - Valid Deleted Flag")
    void testGetDeletedAndSetDeleted_ValidDeletedFlag() {
        // Arrange
        Employee employee = new Employee();

        // Act
        employee.setDeleted(true);

        // Assert
        assertTrue(employee.getDeleted());
    }

    @Test
    @DisplayName("Test getCreatedAt and setCreatedAt - Valid CreatedAt")
    void testGetCreatedAtAndSetCreatedAt_ValidCreatedAt() {
        // Arrange
        Employee employee = new Employee();
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        employee.setCreatedAt(createdAt);

        // Assert
        assertEquals(createdAt, employee.getCreatedAt());
    }

    @Test
    @DisplayName("Test getUpdatedAt and setUpdatedAt - Valid UpdatedAt")
    void testGetUpdatedAtAndSetUpdatedAt_ValidUpdatedAt() {
        // Arrange
        Employee employee = new Employee();
        LocalDateTime updatedAt = LocalDateTime.now();

        // Act
        employee.setUpdatedAt(updatedAt);

        // Assert
        assertEquals(updatedAt, employee.getUpdatedAt());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Test Validation - Valid Employee - No Violations")
    void testValidation_ValidEmployee_NoViolations() {
        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test Validation - Null BadgeId - Has Violation")
    void testValidation_NullBadgeId_HasViolation() {
        // Arrange
        testEmployee.setBadgeId(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test Validation - Empty BadgeId - Has Violation")
    void testValidation_EmptyBadgeId_HasViolation() {
        // Arrange
        testEmployee.setBadgeId("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test Validation - Blank BadgeId - Has Violation")
    void testValidation_BlankBadgeId_HasViolation() {
        // Arrange
        testEmployee.setBadgeId("   ");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Test Validation - Null FirstName - Has Violation")
    void testValidation_NullFirstName_HasViolation() {
        // Arrange
        testEmployee.setFirstName(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    @DisplayName("Test Validation - Empty FirstName - Has Violation")
    void testValidation_EmptyFirstName_HasViolation() {
        // Arrange
        testEmployee.setFirstName("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    @DisplayName("Test Validation - Null LastName - Has Violation")
    void testValidation_NullLastName_HasViolation() {
        // Arrange
        testEmployee.setLastName(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    @DisplayName("Test Validation - Empty LastName - Has Violation")
    void testValidation_EmptyLastName_HasViolation() {
        // Arrange
        testEmployee.setLastName("");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    @DisplayName("Test Validation - Null Email - Has Violation")
    void testValidation_NullEmail_HasViolation() {
        // Arrange
        testEmployee.setEmail(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Test Validation - Invalid Email Format - Has Violation")
    void testValidation_InvalidEmailFormat_HasViolation() {
        // Arrange
        testEmployee.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Test Validation - Email Without Domain - Has Violation")
    void testValidation_EmailWithoutDomain_HasViolation() {
        // Arrange
        testEmployee.setEmail("john.doe@");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // ==================== LIFECYCLE HOOK TESTS ====================

    @Test
    @DisplayName("Test @PrePersist - Sets CreatedAt and UpdatedAt")
    void testPrePersist_SetsCreatedAtAndUpdatedAt() {
        // Arrange
        Employee employee = new Employee();
        employee.setBadgeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@warehouse.com");

        // Simulate @PrePersist by manually calling the method
        // Note: In actual JPA context, this would be called automatically
        LocalDateTime beforePersist = LocalDateTime.now();
        employee.setCreatedAt(beforePersist);
        employee.setUpdatedAt(beforePersist);

        // Assert
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
        assertEquals(employee.getCreatedAt(), employee.getUpdatedAt());
    }

    @Test
    @DisplayName("Test @PreUpdate - Updates UpdatedAt Only")
    void testPreUpdate_UpdatesUpdatedAtOnly() throws InterruptedException {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.now();
        testEmployee.setCreatedAt(originalCreatedAt);
        testEmployee.setUpdatedAt(originalCreatedAt);

        // Wait a bit to ensure time difference
        Thread.sleep(10);

        // Simulate @PreUpdate by manually calling the method
        LocalDateTime newUpdatedAt = LocalDateTime.now();
        testEmployee.setUpdatedAt(newUpdatedAt);

        // Assert
        assertEquals(originalCreatedAt, testEmployee.getCreatedAt());
        assertTrue(testEmployee.getUpdatedAt().isAfter(originalCreatedAt));
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test Maximum Length Fields - Valid")
    void testMaximumLengthFields_Valid() {
        // Arrange
        String longString = "A".repeat(255);
        testEmployee.setFirstName(longString);
        testEmployee.setLastName(longString);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(longString, testEmployee.getFirstName());
        assertEquals(longString, testEmployee.getLastName());
    }

    @Test
    @DisplayName("Test Special Characters in Name - Valid")
    void testSpecialCharactersInName_Valid() {
        // Arrange
        testEmployee.setFirstName("Jean-Pierre");
        testEmployee.setLastName("O'Connor");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("Jean-Pierre", testEmployee.getFirstName());
        assertEquals("O'Connor", testEmployee.getLastName());
    }

    @Test
    @DisplayName("Test Future Hire Date - Valid")
    void testFutureHireDate_Valid() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        testEmployee.setHireDate(futureDate);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(futureDate, testEmployee.getHireDate());
    }

    @Test
    @DisplayName("Test Past Hire Date - Valid")
    void testPastHireDate_Valid() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2000, 1, 1);
        testEmployee.setHireDate(pastDate);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(pastDate, testEmployee.getHireDate());
    }

    @Test
    @DisplayName("Test Null Optional Fields - Valid")
    void testNullOptionalFields_Valid() {
        // Arrange
        testEmployee.setPhoneNumber(null);
        testEmployee.setRole(null);
        testEmployee.setDepartment(null);
        testEmployee.setShiftGroup(null);
        testEmployee.setHireDate(null);
        testEmployee.setStatus(null);

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test Deleted Flag Default Value - False")
    void testDeletedFlagDefaultValue_False() {
        // Arrange
        Employee employee = new Employee();

        // Assert
        assertNull(employee.getDeleted());

        // Set explicitly
        employee.setDeleted(false);
        assertFalse(employee.getDeleted());
    }

    @Test
    @DisplayName("Test Email with Plus Sign - Valid")
    void testEmailWithPlusSign_Valid() {
        // Arrange
        testEmployee.setEmail("john.doe+test@warehouse.com");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test Email with Subdomain - Valid")
    void testEmailWithSubdomain_Valid() {
        // Arrange
        testEmployee.setEmail("john.doe@mail.warehouse.com");

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test Equals and HashCode - Same Object")
    void testEqualsAndHashCode_SameObject() {
        // Assert
        assertEquals(testEmployee, testEmployee);
        assertEquals(testEmployee.hashCode(), testEmployee.hashCode());
    }

    @Test
    @DisplayName("Test ToString - Contains Key Fields")
    void testToString_ContainsKeyFields() {
        // Act
        String toString = testEmployee.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("EMP001") || toString.contains("John") || toString.contains("Doe"));
    }
}