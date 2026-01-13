package com.warehouse.management.dto;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;

/**
 * Comprehensive JUnit 5 test class for EmployeeDTO.
 * Tests cover normal cases, validation, boundaries, and edge cases.
 */
@DisplayName("EmployeeDTO Tests")
public class EmployeeDTOTest {

    private Validator validator;
    private EmployeeDTO testDTO;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testDTO = new EmployeeDTO();
    }

    @AfterEach
    public void tearDown() {
        testDTO = null;
    }

    @Nested
    @DisplayName("Normal Case Tests")
    class NormalCaseTests {

        @Test
        @DisplayName("Test DTO creation with valid data should succeed")
        public void testDTOCreation_WithValidData_ShouldSucceed() {
            // Arrange & Act
            EmployeeDTO dto = new EmployeeDTO();
            dto.setBadgeId("EMP001");
            dto.setFirstName("John");
            dto.setLastName("Doe");
            dto.setEmail("john.doe@warehouse.com");
            dto.setPhoneNumber("+1234567890");
            dto.setRole("WORKER");
            dto.setDepartment("Shipping");
            dto.setShiftGroup("A");
            dto.setHireDate(LocalDate.now());
            dto.setStatus("ACTIVE");

            // Assert
            assertNotNull(dto);
            assertEquals("EMP001", dto.getBadgeId());
            assertEquals("John", dto.getFirstName());
            assertEquals("Doe", dto.getLastName());
            assertEquals("john.doe@warehouse.com", dto.getEmail());
            assertEquals("+1234567890", dto.getPhoneNumber());
            assertEquals("WORKER", dto.getRole());
            assertEquals("Shipping", dto.getDepartment());
            assertEquals("A", dto.getShiftGroup());
            assertNotNull(dto.getHireDate());
            assertEquals("ACTIVE", dto.getStatus());
        }

        @Test
        @DisplayName("Test all getters and setters should work correctly")
        public void testGettersAndSetters_WithValidData_ShouldWork() {
            // Arrange
            Long id = 1L;
            String badgeId = "EMP002";
            String firstName = "Jane";
            String lastName = "Smith";
            String email = "jane.smith@warehouse.com";
            String phoneNumber = "+0987654321";
            String role = "SUPERVISOR";
            String department = "Receiving";
            String shiftGroup = "B";
            LocalDate hireDate = LocalDate.of(2023, 1, 15);
            String status = "ACTIVE";

            // Act
            testDTO.setId(id);
            testDTO.setBadgeId(badgeId);
            testDTO.setFirstName(firstName);
            testDTO.setLastName(lastName);
            testDTO.setEmail(email);
            testDTO.setPhoneNumber(phoneNumber);
            testDTO.setRole(role);
            testDTO.setDepartment(department);
            testDTO.setShiftGroup(shiftGroup);
            testDTO.setHireDate(hireDate);
            testDTO.setStatus(status);

            // Assert
            assertEquals(id, testDTO.getId());
            assertEquals(badgeId, testDTO.getBadgeId());
            assertEquals(firstName, testDTO.getFirstName());
            assertEquals(lastName, testDTO.getLastName());
            assertEquals(email, testDTO.getEmail());
            assertEquals(phoneNumber, testDTO.getPhoneNumber());
            assertEquals(role, testDTO.getRole());
            assertEquals(department, testDTO.getDepartment());
            assertEquals(shiftGroup, testDTO.getShiftGroup());
            assertEquals(hireDate, testDTO.getHireDate());
            assertEquals(status, testDTO.getStatus());
        }

        @Test
        @DisplayName("Test DTO with minimum required fields should succeed")
        public void testDTOCreation_WithMinimumRequiredFields_ShouldSucceed() {
            // Arrange & Act
            EmployeeDTO dto = new EmployeeDTO();
            dto.setBadgeId("E");
            dto.setFirstName("A");
            dto.setLastName("B");
            dto.setRole("W");
            dto.setDepartment("D");
            dto.setHireDate(LocalDate.now());
            dto.setStatus("A");

            // Assert
            assertNotNull(dto);
            assertEquals("E", dto.getBadgeId());
            assertEquals("A", dto.getFirstName());
            assertEquals("B", dto.getLastName());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test validation with null badgeId should fail")
        public void testValidation_WithNullBadgeId_ShouldFail() {
            // Arrange
            testDTO.setBadgeId(null);
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
        }

        @Test
        @DisplayName("Test validation with blank badgeId should fail")
        public void testValidation_WithBlankBadgeId_ShouldFail() {
            // Arrange
            testDTO.setBadgeId("   ");
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test validation with null firstName should fail")
        public void testValidation_WithNullFirstName_ShouldFail() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName(null);
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
        }

        @Test
        @DisplayName("Test validation with blank firstName should fail")
        public void testValidation_WithBlankFirstName_ShouldFail() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName("");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test validation with invalid email format should fail")
        public void testValidation_WithInvalidEmailFormat_ShouldFail() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setEmail("invalid-email");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Test validation with null hireDate should fail")
        public void testValidation_WithNullHireDate_ShouldFail() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(null);
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
        }

        @Test
        @DisplayName("Test validation with all valid fields should pass")
        public void testValidation_WithAllValidFields_ShouldPass() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setEmail("john.doe@warehouse.com");
            testDTO.setPhoneNumber("+1234567890");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setShiftGroup("A");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Test badgeId with minimum valid length should succeed")
        public void testBadgeId_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testDTO.setBadgeId("E");

            // Assert
            assertEquals("E", testDTO.getBadgeId());
            assertEquals(1, testDTO.getBadgeId().length());
        }

        @Test
        @DisplayName("Test badgeId with maximum valid length should succeed")
        public void testBadgeId_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthBadgeId = "E".repeat(50);

            // Act
            testDTO.setBadgeId(maxLengthBadgeId);

            // Assert
            assertEquals(maxLengthBadgeId, testDTO.getBadgeId());
            assertEquals(50, testDTO.getBadgeId().length());
        }

        @Test
        @DisplayName("Test badgeId with length above maximum should be handled")
        public void testBadgeId_WithLengthAboveMaximum_ShouldBeHandled() {
            // Arrange
            String tooLongBadgeId = "E".repeat(51);
            testDTO.setBadgeId(tooLongBadgeId);
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
        }

        @Test
        @DisplayName("Test firstName with minimum valid length should succeed")
        public void testFirstName_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testDTO.setFirstName("A");

            // Assert
            assertEquals("A", testDTO.getFirstName());
            assertEquals(1, testDTO.getFirstName().length());
        }

        @Test
        @DisplayName("Test firstName with maximum valid length should succeed")
        public void testFirstName_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthFirstName = "A".repeat(100);

            // Act
            testDTO.setFirstName(maxLengthFirstName);

            // Assert
            assertEquals(maxLengthFirstName, testDTO.getFirstName());
            assertEquals(100, testDTO.getFirstName().length());
        }

        @Test
        @DisplayName("Test lastName with minimum valid length should succeed")
        public void testLastName_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testDTO.setLastName("B");

            // Assert
            assertEquals("B", testDTO.getLastName());
            assertEquals(1, testDTO.getLastName().length());
        }

        @Test
        @DisplayName("Test lastName with maximum valid length should succeed")
        public void testLastName_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthLastName = "B".repeat(100);

            // Act
            testDTO.setLastName(maxLengthLastName);

            // Assert
            assertEquals(maxLengthLastName, testDTO.getLastName());
            assertEquals(100, testDTO.getLastName().length());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Test badgeId with empty string should fail validation")
        public void testBadgeId_WithEmptyString_ShouldFailValidation() {
            // Arrange
            testDTO.setBadgeId("");
            testDTO.setFirstName("John");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test firstName with whitespace only should fail validation")
        public void testFirstName_WithWhitespaceOnly_ShouldFailValidation() {
            // Arrange
            testDTO.setBadgeId("EMP001");
            testDTO.setFirstName("   ");
            testDTO.setLastName("Doe");
            testDTO.setRole("WORKER");
            testDTO.setDepartment("Shipping");
            testDTO.setHireDate(LocalDate.now());
            testDTO.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(testDTO);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test lastName with special characters should succeed")
        public void testLastName_WithSpecialCharacters_ShouldSucceed() {
            // Arrange
            String specialCharsLastName = "O'Brien-Smith";

            // Act
            testDTO.setLastName(specialCharsLastName);

            // Assert
            assertEquals(specialCharsLastName, testDTO.getLastName());
        }

        @Test
        @DisplayName("Test email with plus sign should succeed")
        public void testEmail_WithPlusSign_ShouldSucceed() {
            // Arrange
            String emailWithPlus = "john.doe+test@warehouse.com";

            // Act
            testDTO.setEmail(emailWithPlus);

            // Assert
            assertEquals(emailWithPlus, testDTO.getEmail());
        }

        @Test
        @DisplayName("Test phoneNumber with various formats should succeed")
        public void testPhoneNumber_WithVariousFormats_ShouldSucceed() {
            // Arrange
            String[] phoneFormats = {
                "+1234567890",
                "(123) 456-7890",
                "123-456-7890",
                "123.456.7890"
            };

            // Act & Assert
            for (String phone : phoneFormats) {
                testDTO.setPhoneNumber(phone);
                assertEquals(phone, testDTO.getPhoneNumber());
            }
        }

        @Test
        @DisplayName("Test role with uppercase should succeed")
        public void testRole_WithUppercase_ShouldSucceed() {
            // Arrange & Act
            testDTO.setRole("ADMIN");

            // Assert
            assertEquals("ADMIN", testDTO.getRole());
        }

        @Test
        @DisplayName("Test department with mixed case should succeed")
        public void testDepartment_WithMixedCase_ShouldSucceed() {
            // Arrange & Act
            testDTO.setDepartment("Shipping & Receiving");

            // Assert
            assertEquals("Shipping & Receiving", testDTO.getDepartment());
        }

        @Test
        @DisplayName("Test status with various values should succeed")
        public void testStatus_WithVariousValues_ShouldSucceed() {
            // Arrange
            String[] statuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};

            // Act & Assert
            for (String status : statuses) {
                testDTO.setStatus(status);
                assertEquals(status, testDTO.getStatus());
            }
        }

        @Test
        @DisplayName("Test optional fields with null should be handled")
        public void testOptionalFields_WithNull_ShouldBeHandled() {
            // Arrange & Act
            testDTO.setEmail(null);
            testDTO.setPhoneNumber(null);
            testDTO.setShiftGroup(null);

            // Assert
            assertAll(
                () -> assertNull(testDTO.getEmail()),
                () -> assertNull(testDTO.getPhoneNumber()),
                () -> assertNull(testDTO.getShiftGroup())
            );
        }

        @Test
        @DisplayName("Test hireDate with current date should succeed")
        public void testHireDate_WithCurrentDate_ShouldSucceed() {
            // Arrange
            LocalDate today = LocalDate.now();

            // Act
            testDTO.setHireDate(today);

            // Assert
            assertEquals(today, testDTO.getHireDate());
        }

        @Test
        @DisplayName("Test hireDate with past date should succeed")
        public void testHireDate_WithPastDate_ShouldSucceed() {
            // Arrange
            LocalDate pastDate = LocalDate.of(2020, 1, 1);

            // Act
            testDTO.setHireDate(pastDate);

            // Assert
            assertEquals(pastDate, testDTO.getHireDate());
            assertTrue(testDTO.getHireDate().isBefore(LocalDate.now()));
        }

        @Test
        @DisplayName("Test hireDate with future date should be handled")
        public void testHireDate_WithFutureDate_ShouldBeHandled() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(30);

            // Act
            testDTO.setHireDate(futureDate);

            // Assert
            assertEquals(futureDate, testDTO.getHireDate());
            assertTrue(testDTO.getHireDate().isAfter(LocalDate.now()));
        }
    }
}
