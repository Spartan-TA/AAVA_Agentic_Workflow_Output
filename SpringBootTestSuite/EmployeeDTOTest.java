package com.warehouse.dto;

import com.warehouse.employee.EmployeeRole;
import com.warehouse.employee.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeDTO
 * Tests cover all validation constraints with normal cases, boundary conditions, and edge cases
 */
public class EmployeeDTOTest {

    private Validator validator;
    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Arrange: Set up validator and valid test data
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@warehouse.com");
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setShiftGroup("Morning");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== VALID DTO TESTS ==========

    @Test
    public void testValidEmployeeDTO_ShouldPassValidation() {
        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation errors");
    }

    // ========== BADGE ID VALIDATION TESTS ==========

    @Test
    public void testBadgeId_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setBadgeId(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    public void testBadgeId_WithEmptyString_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setBadgeId("");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    public void testBadgeId_WithWhitespace_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setBadgeId("   ");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testBadgeId_WithValidFormat_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setBadgeId("EMP12345");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testBadgeId_WithMaxLength_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setBadgeId("A".repeat(20)); // Assuming max length is 20

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== FIRST NAME VALIDATION TESTS ==========

    @Test
    public void testFirstName_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setFirstName(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    @Test
    public void testFirstName_WithEmptyString_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setFirstName("");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFirstName_WithWhitespace_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setFirstName("   ");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFirstName_WithMinLength_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setFirstName("A");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testFirstName_WithMaxLength_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setFirstName("A".repeat(50));

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testFirstName_WithSpecialCharacters_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setFirstName("O'Brien");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== LAST NAME VALIDATION TESTS ==========

    @Test
    public void testLastName_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setLastName(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    @Test
    public void testLastName_WithEmptyString_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setLastName("");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testLastName_WithHyphen_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setLastName("Smith-Jones");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== EMAIL VALIDATION TESTS ==========

    @Test
    public void testEmail_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setEmail(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testEmail_WithInvalidFormat_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    public void testEmail_WithMissingAtSymbol_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setEmail("invalidemail.com");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmail_WithMissingDomain_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setEmail("invalid@");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmail_WithValidFormat_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setEmail("john.doe@warehouse.com");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmail_WithSubdomain_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setEmail("john.doe@mail.warehouse.com");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testEmail_WithPlusSign_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setEmail("john.doe+test@warehouse.com");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== ROLE VALIDATION TESTS ==========

    @Test
    public void testRole_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setRole(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    public void testRole_WithAllValidRoles_ShouldPassValidation() {
        // Test ADMIN
        validEmployeeDTO.setRole(EmployeeRole.ADMIN);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test HR
        validEmployeeDTO.setRole(EmployeeRole.HR);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test SUPERVISOR
        validEmployeeDTO.setRole(EmployeeRole.SUPERVISOR);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test WORKER
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());
    }

    // ========== DEPARTMENT VALIDATION TESTS ==========

    @Test
    public void testDepartment_WithNull_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setDepartment(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty(), "Department is optional");
    }

    @Test
    public void testDepartment_WithValidValue_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setDepartment("Logistics");

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    // ========== STATUS VALIDATION TESTS ==========

    @Test
    public void testStatus_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setStatus(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    public void testStatus_WithAllValidStatuses_ShouldPassValidation() {
        // Test ACTIVE
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test INACTIVE
        validEmployeeDTO.setStatus(EmployeeStatus.INACTIVE);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test ON_LEAVE
        validEmployeeDTO.setStatus(EmployeeStatus.ON_LEAVE);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());

        // Test TERMINATED
        validEmployeeDTO.setStatus(EmployeeStatus.TERMINATED);
        assertTrue(validator.validate(validEmployeeDTO).isEmpty());
    }

    // ========== HIRE DATE VALIDATION TESTS ==========

    @Test
    public void testHireDate_WithNull_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setHireDate(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    public void testHireDate_WithPastDate_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now().minusYears(5));

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testHireDate_WithCurrentDate_ShouldPassValidation() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now());

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testHireDate_WithFutureDate_ShouldFailValidation() {
        // Arrange
        validEmployeeDTO.setHireDate(LocalDate.now().plusDays(1));

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(validEmployeeDTO);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    // ========== GETTER/SETTER TESTS ==========

    @Test
    public void testGettersAndSetters_ShouldWorkCorrectly() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        Long id = 1L;
        String badgeId = "EMP001";
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@warehouse.com";
        EmployeeRole role = EmployeeRole.WORKER;
        String department = "Warehouse";
        String shiftGroup = "Morning";
        LocalDate hireDate = LocalDate.now();
        EmployeeStatus status = EmployeeStatus.ACTIVE;

        // Act
        dto.setId(id);
        dto.setBadgeId(badgeId);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setDepartment(department);
        dto.setShiftGroup(shiftGroup);
        dto.setHireDate(hireDate);
        dto.setStatus(status);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(badgeId, dto.getBadgeId());
        assertEquals(firstName, dto.getFirstName());
        assertEquals(lastName, dto.getLastName());
        assertEquals(email, dto.getEmail());
        assertEquals(role, dto.getRole());
        assertEquals(department, dto.getDepartment());
        assertEquals(shiftGroup, dto.getShiftGroup());
        assertEquals(hireDate, dto.getHireDate());
        assertEquals(status, dto.getStatus());
    }

    // ========== EQUALS AND HASHCODE TESTS ==========

    @Test
    public void testEquals_WithSameValues_ShouldReturnTrue() {
        // Arrange
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setId(1L);
        dto1.setBadgeId("EMP001");

        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setId(1L);
        dto2.setBadgeId("EMP001");

        // Act & Assert
        assertEquals(dto1, dto2);
    }

    @Test
    public void testEquals_WithDifferentValues_ShouldReturnFalse() {
        // Arrange
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setId(1L);
        dto1.setBadgeId("EMP001");

        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setId(2L);
        dto2.setBadgeId("EMP002");

        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testHashCode_WithSameValues_ShouldReturnSameHashCode() {
        // Arrange
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setId(1L);
        dto1.setBadgeId("EMP001");

        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setId(1L);
        dto2.setBadgeId("EMP001");

        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testMultipleValidationErrors_ShouldReturnAllViolations() {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId(null);
        invalidDTO.setFirstName(null);
        invalidDTO.setLastName(null);
        invalidDTO.setEmail("invalid");
        invalidDTO.setRole(null);
        invalidDTO.setStatus(null);
        invalidDTO.setHireDate(null);

        // Act
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(invalidDTO);

        // Assert
        assertTrue(violations.size() >= 6, "Should have multiple validation errors");
    }

    @Test
    public void testToString_ShouldReturnNonNullString() {
        // Act
        String result = validEmployeeDTO.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("EMP001"));
    }
}