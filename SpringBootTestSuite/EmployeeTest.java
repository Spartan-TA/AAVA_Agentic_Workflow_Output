package com.company.wems.employee.entity;

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
 * Comprehensive JUnit test suite for Employee entity
 * Tests cover normal cases, boundary conditions, and edge cases
 * including validation, builder pattern, soft delete, and field constraints
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    @DisplayName("Test employee creation with valid data should succeed")
    public void testEmployeeCreation_WithValidData_ShouldSucceed() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("John Doe")
                .badgeId("EMP001")
                .email("john.doe@company.com")
                .phone("+1-555-0100")
                .role("WORKER")
                .department("Warehouse Operations")
                .shiftGroup("Morning Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals("John Doe", employee.getName());
        assertEquals("EMP001", employee.getBadgeId());
        assertEquals("john.doe@company.com", employee.getEmail());
        assertEquals("WORKER", employee.getRole());
        assertEquals("ACTIVE", employee.getStatus());
        assertFalse(employee.getDeleted());
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Valid employee should have no validation violations");
    }

    @Test
    @DisplayName("Test employee creation with minimal required fields should succeed")
    public void testEmployeeCreation_WithMinimalRequiredFields_ShouldSucceed() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals("Jane Smith", employee.getName());
        assertEquals("EMP002", employee.getBadgeId());
        assertNull(employee.getEmail());
        assertNull(employee.getPhone());
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Employee with required fields should have no violations");
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Test employee with null name should fail validation")
    public void testEmployeeValidation_WithNullName_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name(null)
                .badgeId("EMP003")
                .role("WORKER")
                .department("Shipping")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Null name should cause validation violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Violation should be for name field");
    }

    @Test
    @DisplayName("Test employee with empty name should fail validation")
    public void testEmployeeValidation_WithEmptyName_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name("")
                .badgeId("EMP004")
                .role("WORKER")
                .department("Receiving")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Empty name should cause validation violation");
    }

    @Test
    @DisplayName("Test employee with blank name should fail validation")
    public void testEmployeeValidation_WithBlankName_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name("   ")
                .badgeId("EMP005")
                .role("WORKER")
                .department("Quality Control")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Blank name should cause validation violation");
    }

    @Test
    @DisplayName("Test employee with null badgeId should fail validation")
    public void testEmployeeValidation_WithNullBadgeId_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId(null)
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Null badgeId should cause validation violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")),
                "Violation should be for badgeId field");
    }

    @Test
    @DisplayName("Test employee with invalid email format should fail validation")
    public void testEmployeeValidation_WithInvalidEmail_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP006")
                .email("invalid-email")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Invalid email should cause validation violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                "Violation should be for email field");
    }

    @Test
    @DisplayName("Test employee with null hireDate should fail validation")
    public void testEmployeeValidation_WithNullHireDate_ShouldFail() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP007")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(null)
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertFalse(violations.isEmpty(), "Null hireDate should cause validation violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")),
                "Violation should be for hireDate field");
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @DisplayName("Test employee with maximum length name should succeed")
    public void testEmployeeCreation_WithMaxLengthName_ShouldSucceed() {
        // Arrange - Create 100 character name (max length)
        String maxLengthName = "A".repeat(100);
        
        Employee employee = Employee.builder()
                .name(maxLengthName)
                .badgeId("EMP008")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Max length name should not cause violation");
        assertEquals(100, employee.getName().length());
    }

    @Test
    @DisplayName("Test employee with maximum length badgeId should succeed")
    public void testEmployeeCreation_WithMaxLengthBadgeId_ShouldSucceed() {
        // Arrange - Create 50 character badgeId (max length)
        String maxLengthBadgeId = "B".repeat(50);
        
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId(maxLengthBadgeId)
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Max length badgeId should not cause violation");
        assertEquals(50, employee.getBadgeId().length());
    }

    @Test
    @DisplayName("Test employee with past hire date should succeed")
    public void testEmployeeCreation_WithPastHireDate_ShouldSucceed() {
        // Arrange
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP009")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(pastDate)
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Past hire date should be valid");
        assertEquals(pastDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee with current date as hire date should succeed")
    public void testEmployeeCreation_WithCurrentHireDate_ShouldSucceed() {
        // Arrange
        LocalDate currentDate = LocalDate.now();
        
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP010")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(currentDate)
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Current hire date should be valid");
        assertEquals(currentDate, employee.getHireDate());
    }

    @Test
    @DisplayName("Test employee with future hire date should succeed")
    public void testEmployeeCreation_WithFutureHireDate_ShouldSucceed() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP011")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(futureDate)
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Future hire date should be valid");
        assertEquals(futureDate, employee.getHireDate());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test employee with special characters in name should succeed")
    public void testEmployeeCreation_WithSpecialCharactersInName_ShouldSucceed() {
        // Arrange
        Employee employee = Employee.builder()
                .name("O'Brien-Smith, Jr.")
                .badgeId("EMP012")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Special characters in name should be valid");
        assertEquals("O'Brien-Smith, Jr.", employee.getName());
    }

    @Test
    @DisplayName("Test employee with unicode characters in name should succeed")
    public void testEmployeeCreation_WithUnicodeCharactersInName_ShouldSucceed() {
        // Arrange
        Employee employee = Employee.builder()
                .name("JosÃ© GarcÃ­a")
                .badgeId("EMP013")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        // Act
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        // Assert
        assertTrue(violations.isEmpty(), "Unicode characters in name should be valid");
        assertEquals("JosÃ© GarcÃ­a", employee.getName());
    }

    @Test
    @DisplayName("Test employee with various email formats should validate correctly")
    public void testEmployeeValidation_WithVariousEmailFormats_ShouldValidateCorrectly() {
        // Test valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com"
        };

        for (String email : validEmails) {
            Employee employee = Employee.builder()
                    .name("Test Employee")
                    .badgeId("EMP" + email.hashCode())
                    .email(email)
                    .role("WORKER")
                    .department("Warehouse")
                    .hireDate(LocalDate.now())
                    .status("ACTIVE")
                    .build();

            Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }

    @Test
    @DisplayName("Test employee soft delete flag should work correctly")
    public void testEmployeeSoftDelete_ShouldWorkCorrectly() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP014")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .build();

        // Act - Soft delete
        employee.setDeleted(true);

        // Assert
        assertTrue(employee.getDeleted(), "Employee should be marked as deleted");
        assertNotNull(employee.getName(), "Employee data should still exist after soft delete");
    }

    @Test
    @DisplayName("Test employee with all optional fields populated should succeed")
    public void testEmployeeCreation_WithAllOptionalFields_ShouldSucceed() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Complete Employee")
                .badgeId("EMP015")
                .email("complete@company.com")
                .phone("+1-555-0199")
                .role("SUPERVISOR")
                .department("Operations")
                .shiftGroup("Evening Shift")
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE")
                .deleted(false)
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .country("USA")
                .emergencyContactName("Jane Doe")
                .emergencyContactPhone("+1-555-0200")
                .build();

        // Assert
        assertNotNull(employee);
        assertEquals("123 Main St", employee.getAddressLine1());
        assertEquals("Apt 4B", employee.getAddressLine2());
        assertEquals("Springfield", employee.getCity());
        assertEquals("IL", employee.getState());
        assertEquals("62701", employee.getPostalCode());
        assertEquals("USA", employee.getCountry());
        assertEquals("Jane Doe", employee.getEmergencyContactName());
        assertEquals("+1-555-0200", employee.getEmergencyContactPhone());
        
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Employee with all fields should have no violations");
    }

    @Test
    @DisplayName("Test employee with different status values should succeed")
    public void testEmployeeCreation_WithDifferentStatusValues_ShouldSucceed() {
        // Test various status values
        String[] statuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};

        for (String status : statuses) {
            Employee employee = Employee.builder()
                    .name("Test Employee")
                    .badgeId("EMP" + status)
                    .role("WORKER")
                    .department("Warehouse")
                    .hireDate(LocalDate.now())
                    .status(status)
                    .build();

            Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
            assertTrue(violations.isEmpty(), "Status " + status + " should be valid");
            assertEquals(status, employee.getStatus());
        }
    }

    @Test
    @DisplayName("Test employee with different role values should succeed")
    public void testEmployeeCreation_WithDifferentRoleValues_ShouldSucceed() {
        // Test various role values
        String[] roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"};

        for (String role : roles) {
            Employee employee = Employee.builder()
                    .name("Test Employee")
                    .badgeId("EMP" + role)
                    .role(role)
                    .department("Warehouse")
                    .hireDate(LocalDate.now())
                    .status("ACTIVE")
                    .build();

            Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
            assertTrue(violations.isEmpty(), "Role " + role + " should be valid");
            assertEquals(role, employee.getRole());
        }
    }

    @Test
    @DisplayName("Test employee builder pattern should create identical objects")
    public void testEmployeeBuilder_ShouldCreateIdenticalObjects() {
        // Arrange & Act
        Employee employee1 = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP016")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.of(2023, 1, 1))
                .status("ACTIVE")
                .build();

        Employee employee2 = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP016")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.of(2023, 1, 1))
                .status("ACTIVE")
                .build();

        // Assert
        assertEquals(employee1.getName(), employee2.getName());
        assertEquals(employee1.getBadgeId(), employee2.getBadgeId());
        assertEquals(employee1.getRole(), employee2.getRole());
        assertEquals(employee1.getDepartment(), employee2.getDepartment());
        assertEquals(employee1.getHireDate(), employee2.getHireDate());
        assertEquals(employee1.getStatus(), employee2.getStatus());
    }

    @Test
    @DisplayName("Test employee with null optional fields should succeed")
    public void testEmployeeCreation_WithNullOptionalFields_ShouldSucceed() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .name("Test Employee")
                .badgeId("EMP017")
                .email(null)
                .phone(null)
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup(null)
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .addressLine1(null)
                .addressLine2(null)
                .city(null)
                .state(null)
                .postalCode(null)
                .country(null)
                .emergencyContactName(null)
                .emergencyContactPhone(null)
                .build();

        // Assert
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Null optional fields should not cause violations");
        assertNull(employee.getEmail());
        assertNull(employee.getPhone());
        assertNull(employee.getShiftGroup());
    }
}