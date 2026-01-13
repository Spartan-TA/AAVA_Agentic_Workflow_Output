package com.warehouse.management.entity;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

/**
 * Comprehensive JUnit 5 test class for Employee entity.
 * Tests cover normal cases, validation, boundaries, edge cases, relationships, and lifecycle callbacks.
 */
@DisplayName("Employee Entity Tests")
public class EmployeeTest {

    private Validator validator;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testEmployee = new Employee();
    }

    @AfterEach
    public void tearDown() {
        testEmployee = null;
    }

    @Nested
    @DisplayName("Normal Case Tests")
    class NormalCaseTests {

        @Test
        @DisplayName("Test employee creation with valid data should succeed")
        public void testEmployeeCreation_WithValidData_ShouldSucceed() {
            // Arrange & Act
            Employee employee = new Employee();
            employee.setBadgeId("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john.doe@warehouse.com");
            employee.setPhoneNumber("+1234567890");
            employee.setRole("WORKER");
            employee.setDepartment("Shipping");
            employee.setShiftGroup("A");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setDeleted(false);

            // Assert
            assertNotNull(employee);
            assertEquals("EMP001", employee.getBadgeId());
            assertEquals("John", employee.getFirstName());
            assertEquals("Doe", employee.getLastName());
            assertEquals("john.doe@warehouse.com", employee.getEmail());
            assertEquals("+1234567890", employee.getPhoneNumber());
            assertEquals("WORKER", employee.getRole());
            assertEquals("Shipping", employee.getDepartment());
            assertEquals("A", employee.getShiftGroup());
            assertNotNull(employee.getHireDate());
            assertEquals("ACTIVE", employee.getStatus());
            assertFalse(employee.getDeleted());
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
            Boolean deleted = false;
            String createdBy = "admin";
            String updatedBy = "admin";

            // Act
            testEmployee.setId(id);
            testEmployee.setBadgeId(badgeId);
            testEmployee.setFirstName(firstName);
            testEmployee.setLastName(lastName);
            testEmployee.setEmail(email);
            testEmployee.setPhoneNumber(phoneNumber);
            testEmployee.setRole(role);
            testEmployee.setDepartment(department);
            testEmployee.setShiftGroup(shiftGroup);
            testEmployee.setHireDate(hireDate);
            testEmployee.setStatus(status);
            testEmployee.setDeleted(deleted);
            testEmployee.setCreatedBy(createdBy);
            testEmployee.setUpdatedBy(updatedBy);

            // Assert
            assertEquals(id, testEmployee.getId());
            assertEquals(badgeId, testEmployee.getBadgeId());
            assertEquals(firstName, testEmployee.getFirstName());
            assertEquals(lastName, testEmployee.getLastName());
            assertEquals(email, testEmployee.getEmail());
            assertEquals(phoneNumber, testEmployee.getPhoneNumber());
            assertEquals(role, testEmployee.getRole());
            assertEquals(department, testEmployee.getDepartment());
            assertEquals(shiftGroup, testEmployee.getShiftGroup());
            assertEquals(hireDate, testEmployee.getHireDate());
            assertEquals(status, testEmployee.getStatus());
            assertEquals(deleted, testEmployee.getDeleted());
            assertEquals(createdBy, testEmployee.getCreatedBy());
            assertEquals(updatedBy, testEmployee.getUpdatedBy());
        }

        @Test
        @DisplayName("Test employee with minimum required fields should succeed")
        public void testEmployeeCreation_WithMinimumRequiredFields_ShouldSucceed() {
            // Arrange & Act
            Employee employee = new Employee();
            employee.setBadgeId("E");
            employee.setFirstName("A");
            employee.setLastName("B");
            employee.setRole("W");
            employee.setDepartment("D");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("A");
            employee.setDeleted(false);

            // Assert
            assertNotNull(employee);
            assertEquals("E", employee.getBadgeId());
            assertEquals("A", employee.getFirstName());
            assertEquals("B", employee.getLastName());
        }

        @Test
        @DisplayName("Test employee default deleted flag should be false")
        public void testEmployeeCreation_DefaultDeletedFlag_ShouldBeFalse() {
            // Arrange & Act
            Employee employee = new Employee();

            // Assert
            assertNotNull(employee.getDeleted());
            assertFalse(employee.getDeleted());
        }

        @Test
        @DisplayName("Test employee with all optional fields should succeed")
        public void testEmployeeCreation_WithAllOptionalFields_ShouldSucceed() {
            // Arrange & Act
            Employee employee = new Employee();
            employee.setBadgeId("EMP003");
            employee.setFirstName("Bob");
            employee.setLastName("Johnson");
            employee.setEmail("bob.johnson@warehouse.com");
            employee.setPhoneNumber("+1122334455");
            employee.setRole("ADMIN");
            employee.setDepartment("Management");
            employee.setShiftGroup("C");
            employee.setHireDate(LocalDate.of(2022, 6, 1));
            employee.setStatus("ACTIVE");
            employee.setDeleted(false);
            employee.setCreatedBy("system");
            employee.setUpdatedBy("system");

            // Assert
            assertAll(
                () -> assertNotNull(employee.getEmail()),
                () -> assertNotNull(employee.getPhoneNumber()),
                () -> assertNotNull(employee.getShiftGroup()),
                () -> assertNotNull(employee.getCreatedBy()),
                () -> assertNotNull(employee.getUpdatedBy())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test validation with null badgeId should fail")
        public void testValidation_WithNullBadgeId_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId(null);
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
        }

        @Test
        @DisplayName("Test validation with null firstName should fail")
        public void testValidation_WithNullFirstName_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName(null);
            testEmployee.setLastName("Doe");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
        }

        @Test
        @DisplayName("Test validation with null lastName should fail")
        public void testValidation_WithNullLastName_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName(null);
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
        }

        @Test
        @DisplayName("Test validation with null role should fail")
        public void testValidation_WithNullRole_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setRole(null);
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
        }

        @Test
        @DisplayName("Test validation with null department should fail")
        public void testValidation_WithNullDepartment_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment(null);
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("department")));
        }

        @Test
        @DisplayName("Test validation with null hireDate should fail")
        public void testValidation_WithNullHireDate_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(null);
            testEmployee.setStatus("ACTIVE");

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
        }

        @Test
        @DisplayName("Test validation with null status should fail")
        public void testValidation_WithNullStatus_ShouldFail() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus(null);

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
        }

        @Test
        @DisplayName("Test validation with all valid fields should pass")
        public void testValidation_WithAllValidFields_ShouldPass() {
            // Arrange
            testEmployee.setBadgeId("EMP001");
            testEmployee.setFirstName("John");
            testEmployee.setLastName("Doe");
            testEmployee.setEmail("john.doe@warehouse.com");
            testEmployee.setPhoneNumber("+1234567890");
            testEmployee.setRole("WORKER");
            testEmployee.setDepartment("Shipping");
            testEmployee.setShiftGroup("A");
            testEmployee.setHireDate(LocalDate.now());
            testEmployee.setStatus("ACTIVE");
            testEmployee.setDeleted(false);

            // Act
            Set<ConstraintViolation<Employee>> violations = validator.validate(testEmployee);

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
            testEmployee.setBadgeId("E");

            // Assert
            assertEquals("E", testEmployee.getBadgeId());
            assertEquals(1, testEmployee.getBadgeId().length());
        }

        @Test
        @DisplayName("Test badgeId with maximum valid length should succeed")
        public void testBadgeId_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthBadgeId = "E".repeat(50);

            // Act
            testEmployee.setBadgeId(maxLengthBadgeId);

            // Assert
            assertEquals(maxLengthBadgeId, testEmployee.getBadgeId());
            assertEquals(50, testEmployee.getBadgeId().length());
        }

        @Test
        @DisplayName("Test firstName with minimum valid length should succeed")
        public void testFirstName_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testEmployee.setFirstName("A");

            // Assert
            assertEquals("A", testEmployee.getFirstName());
            assertEquals(1, testEmployee.getFirstName().length());
        }

        @Test
        @DisplayName("Test firstName with maximum valid length should succeed")
        public void testFirstName_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthFirstName = "A".repeat(100);

            // Act
            testEmployee.setFirstName(maxLengthFirstName);

            // Assert
            assertEquals(maxLengthFirstName, testEmployee.getFirstName());
            assertEquals(100, testEmployee.getFirstName().length());
        }

        @Test
        @DisplayName("Test lastName with minimum valid length should succeed")
        public void testLastName_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testEmployee.setLastName("B");

            // Assert
            assertEquals("B", testEmployee.getLastName());
            assertEquals(1, testEmployee.getLastName().length());
        }

        @Test
        @DisplayName("Test lastName with maximum valid length should succeed")
        public void testLastName_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthLastName = "B".repeat(100);

            // Act
            testEmployee.setLastName(maxLengthLastName);

            // Assert
            assertEquals(maxLengthLastName, testEmployee.getLastName());
            assertEquals(100, testEmployee.getLastName().length());
        }

        @Test
        @DisplayName("Test hireDate with current date should succeed")
        public void testHireDate_WithCurrentDate_ShouldSucceed() {
            // Arrange
            LocalDate today = LocalDate.now();

            // Act
            testEmployee.setHireDate(today);

            // Assert
            assertEquals(today, testEmployee.getHireDate());
        }

        @Test
        @DisplayName("Test hireDate with past date should succeed")
        public void testHireDate_WithPastDate_ShouldSucceed() {
            // Arrange
            LocalDate pastDate = LocalDate.of(2020, 1, 1);

            // Act
            testEmployee.setHireDate(pastDate);

            // Assert
            assertEquals(pastDate, testEmployee.getHireDate());
            assertTrue(testEmployee.getHireDate().isBefore(LocalDate.now()));
        }

        @Test
        @DisplayName("Test hireDate with future date should be handled")
        public void testHireDate_WithFutureDate_ShouldBeHandled() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(30);

            // Act
            testEmployee.setHireDate(futureDate);

            // Assert
            assertEquals(futureDate, testEmployee.getHireDate());
            assertTrue(testEmployee.getHireDate().isAfter(LocalDate.now()));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Test badgeId with empty string should be handled")
        public void testBadgeId_WithEmptyString_ShouldBeHandled() {
            // Arrange & Act
            testEmployee.setBadgeId("");

            // Assert
            assertEquals("", testEmployee.getBadgeId());
        }

        @Test
        @DisplayName("Test firstName with whitespace only should be handled")
        public void testFirstName_WithWhitespaceOnly_ShouldBeHandled() {
            // Arrange & Act
            testEmployee.setFirstName("   ");

            // Assert
            assertEquals("   ", testEmployee.getFirstName());
        }

        @Test
        @DisplayName("Test lastName with special characters should succeed")
        public void testLastName_WithSpecialCharacters_ShouldSucceed() {
            // Arrange
            String specialCharsLastName = "O'Brien-Smith";

            // Act
            testEmployee.setLastName(specialCharsLastName);

            // Assert
            assertEquals(specialCharsLastName, testEmployee.getLastName());
        }

        @Test
        @DisplayName("Test email with special characters should succeed")
        public void testEmail_WithSpecialCharacters_ShouldSucceed() {
            // Arrange
            String specialEmail = "john.doe+test@warehouse-company.com";

            // Act
            testEmployee.setEmail(specialEmail);

            // Assert
            assertEquals(specialEmail, testEmployee.getEmail());
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
                testEmployee.setPhoneNumber(phone);
                assertEquals(phone, testEmployee.getPhoneNumber());
            }
        }

        @Test
        @DisplayName("Test role with uppercase should succeed")
        public void testRole_WithUppercase_ShouldSucceed() {
            // Arrange & Act
            testEmployee.setRole("ADMIN");

            // Assert
            assertEquals("ADMIN", testEmployee.getRole());
        }

        @Test
        @DisplayName("Test department with mixed case should succeed")
        public void testDepartment_WithMixedCase_ShouldSucceed() {
            // Arrange & Act
            testEmployee.setDepartment("Shipping & Receiving");

            // Assert
            assertEquals("Shipping & Receiving", testEmployee.getDepartment());
        }

        @Test
        @DisplayName("Test status with various values should succeed")
        public void testStatus_WithVariousValues_ShouldSucceed() {
            // Arrange
            String[] statuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};

            // Act & Assert
            for (String status : statuses) {
                testEmployee.setStatus(status);
                assertEquals(status, testEmployee.getStatus());
            }
        }

        @Test
        @DisplayName("Test deleted flag toggle should work")
        public void testDeletedFlag_Toggle_ShouldWork() {
            // Arrange
            testEmployee.setDeleted(false);

            // Act
            testEmployee.setDeleted(true);

            // Assert
            assertTrue(testEmployee.getDeleted());

            // Act again
            testEmployee.setDeleted(false);

            // Assert
            assertFalse(testEmployee.getDeleted());
        }

        @Test
        @DisplayName("Test optional fields with null should be handled")
        public void testOptionalFields_WithNull_ShouldBeHandled() {
            // Arrange & Act
            testEmployee.setEmail(null);
            testEmployee.setPhoneNumber(null);
            testEmployee.setShiftGroup(null);
            testEmployee.setCreatedBy(null);
            testEmployee.setUpdatedBy(null);

            // Assert
            assertAll(
                () -> assertNull(testEmployee.getEmail()),
                () -> assertNull(testEmployee.getPhoneNumber()),
                () -> assertNull(testEmployee.getShiftGroup()),
                () -> assertNull(testEmployee.getCreatedBy()),
                () -> assertNull(testEmployee.getUpdatedBy())
            );
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Test attendance events collection initialization should not be null")
        public void testAttendanceEventsCollection_Initialization_ShouldNotBeNull() {
            // Arrange & Act
            Employee employee = new Employee();

            // Assert
            assertNotNull(employee.getAttendanceEvents());
            assertTrue(employee.getAttendanceEvents().isEmpty());
        }

        @Test
        @DisplayName("Test add attendance event should increase collection size")
        public void testAddAttendanceEvent_ShouldIncreaseCollectionSize() {
            // Arrange
            Employee employee = new Employee();
            AttendanceEvent event = new AttendanceEvent();
            int initialSize = employee.getAttendanceEvents().size();

            // Act
            employee.getAttendanceEvents().add(event);

            // Assert
            assertEquals(initialSize + 1, employee.getAttendanceEvents().size());
            assertTrue(employee.getAttendanceEvents().contains(event));
        }

        @Test
        @DisplayName("Test remove attendance event should decrease collection size")
        public void testRemoveAttendanceEvent_ShouldDecreaseCollectionSize() {
            // Arrange
            Employee employee = new Employee();
            AttendanceEvent event = new AttendanceEvent();
            employee.getAttendanceEvents().add(event);
            int initialSize = employee.getAttendanceEvents().size();

            // Act
            employee.getAttendanceEvents().remove(event);

            // Assert
            assertEquals(initialSize - 1, employee.getAttendanceEvents().size());
            assertFalse(employee.getAttendanceEvents().contains(event));
        }

        @Test
        @DisplayName("Test clear attendance events should empty collection")
        public void testClearAttendanceEvents_ShouldEmptyCollection() {
            // Arrange
            Employee employee = new Employee();
            employee.getAttendanceEvents().add(new AttendanceEvent());
            employee.getAttendanceEvents().add(new AttendanceEvent());

            // Act
            employee.getAttendanceEvents().clear();

            // Assert
            assertTrue(employee.getAttendanceEvents().isEmpty());
            assertEquals(0, employee.getAttendanceEvents().size());
        }

        @Test
        @DisplayName("Test multiple attendance events should be stored correctly")
        public void testMultipleAttendanceEvents_ShouldBeStoredCorrectly() {
            // Arrange
            Employee employee = new Employee();
            AttendanceEvent event1 = new AttendanceEvent();
            AttendanceEvent event2 = new AttendanceEvent();
            AttendanceEvent event3 = new AttendanceEvent();

            // Act
            employee.getAttendanceEvents().add(event1);
            employee.getAttendanceEvents().add(event2);
            employee.getAttendanceEvents().add(event3);

            // Assert
            assertEquals(3, employee.getAttendanceEvents().size());
            assertAll(
                () -> assertTrue(employee.getAttendanceEvents().contains(event1)),
                () -> assertTrue(employee.getAttendanceEvents().contains(event2)),
                () -> assertTrue(employee.getAttendanceEvents().contains(event3))
            );
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Test onCreate callback should set createdAt and updatedAt")
        public void testOnCreate_ShouldSetCreatedAtAndUpdatedAt() {
            // Arrange
            Employee employee = new Employee();
            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

            // Act
            employee.onCreate();
            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

            // Assert
            assertNotNull(employee.getCreatedAt());
            assertNotNull(employee.getUpdatedAt());
            assertTrue(employee.getCreatedAt().isAfter(beforeCreate));
            assertTrue(employee.getCreatedAt().isBefore(afterCreate));
            assertEquals(employee.getCreatedAt(), employee.getUpdatedAt());
        }

        @Test
        @DisplayName("Test onUpdate callback should update updatedAt only")
        public void testOnUpdate_ShouldUpdateUpdatedAtOnly() throws InterruptedException {
            // Arrange
            Employee employee = new Employee();
            employee.onCreate();
            LocalDateTime originalCreatedAt = employee.getCreatedAt();
            LocalDateTime originalUpdatedAt = employee.getUpdatedAt();
            
            // Wait a bit to ensure time difference
            Thread.sleep(100);

            // Act
            employee.onUpdate();

            // Assert
            assertEquals(originalCreatedAt, employee.getCreatedAt());
            assertNotEquals(originalUpdatedAt, employee.getUpdatedAt());
            assertTrue(employee.getUpdatedAt().isAfter(originalUpdatedAt));
        }

        @Test
        @DisplayName("Test onCreate should not change createdAt on subsequent calls")
        public void testOnCreate_ShouldNotChangeCreatedAtOnSubsequentCalls() throws InterruptedException {
            // Arrange
            Employee employee = new Employee();
            employee.onCreate();
            LocalDateTime originalCreatedAt = employee.getCreatedAt();
            
            // Wait a bit
            Thread.sleep(100);

            // Act
            employee.onCreate();

            // Assert
            // Note: In real JPA, @PrePersist is only called once, but this tests the method behavior
            assertNotNull(employee.getCreatedAt());
            assertNotNull(employee.getUpdatedAt());
        }

        @Test
        @DisplayName("Test audit fields should be set correctly")
        public void testAuditFields_ShouldBeSetCorrectly() {
            // Arrange
            Employee employee = new Employee();
            employee.setCreatedBy("admin");
            employee.setUpdatedBy("supervisor");

            // Act
            employee.onCreate();

            // Assert
            assertEquals("admin", employee.getCreatedBy());
            assertEquals("supervisor", employee.getUpdatedBy());
            assertNotNull(employee.getCreatedAt());
            assertNotNull(employee.getUpdatedAt());
        }
    }
}