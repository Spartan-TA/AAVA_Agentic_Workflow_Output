package com.warehouse.employee.management.entity;

import jakarta.validation.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeEntityTest {
    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    private Employee.EmployeeBuilder validEmployeeBuilder() {
        return Employee.builder()
                .badgeId("BADGE1234567890")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    @Test
    void testValidEmployee_PassesValidation() {
        Employee employee = validEmployeeBuilder().build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullBadgeId_FailsValidation() {
        Employee employee = validEmployeeBuilder().badgeId(null).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testEmptyBadgeId_FailsValidation() {
        Employee employee = validEmployeeBuilder().badgeId("").build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testBadgeIdMaxLength() {
        Employee employee = validEmployeeBuilder().badgeId("A".repeat(32)).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
        employee = validEmployeeBuilder().badgeId("A".repeat(33)).build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testFirstNameMaxLength() {
        Employee employee = validEmployeeBuilder().firstName("A".repeat(64)).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
        employee = validEmployeeBuilder().firstName("A".repeat(65)).build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testLastNameMaxLength() {
        Employee employee = validEmployeeBuilder().lastName("A".repeat(64)).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
        employee = validEmployeeBuilder().lastName("A".repeat(65)).build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEmailValidation() {
        Employee employee = validEmployeeBuilder().email("not-an-email").build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        employee = validEmployeeBuilder().email("a@b.com").build();
        violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailMaxLength() {
        String longEmail = "a".repeat(120) + "@ex.com";
        Employee employee = validEmployeeBuilder().email(longEmail).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
        employee = validEmployeeBuilder().email("a".repeat(121) + "@ex.com").build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testRoleNotBlank() {
        Employee employee = validEmployeeBuilder().role("").build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        employee = validEmployeeBuilder().role(null).build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testStatusNotBlank() {
        Employee employee = validEmployeeBuilder().status("").build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        employee = validEmployeeBuilder().status(null).build();
        violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDepartmentAndShiftGroupNullable() {
        Employee employee = validEmployeeBuilder().department(null).shiftGroup(null).build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLifecycleCallbacks_onCreateAndUpdate() {
        Employee employee = validEmployeeBuilder().createdAt(null).updatedAt(null).build();
        assertNull(employee.getCreatedAt());
        assertNull(employee.getUpdatedAt());
        employee.onCreate();
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
        LocalDateTime oldUpdated = employee.getUpdatedAt();
        employee.onUpdate();
        assertTrue(employee.getUpdatedAt().isAfter(oldUpdated) || employee.getUpdatedAt().isEqual(oldUpdated));
    }

    @Test
    void testDeletedDefaultValue() {
        Employee employee = new Employee();
        assertNull(employee.getDeleted()); // Lombok builder sets default, but constructor does not
        employee = validEmployeeBuilder().deleted(null).build();
        assertNull(employee.getDeleted());
        employee = validEmployeeBuilder().build();
        assertFalse(employee.getDeleted());
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Employee emp1 = new Employee(1L, "BID", "F", "L", "e@e.com", "ADMIN", "Dep", "SG", LocalDate.now(), "ACTIVE", false, now, now);
        assertEquals("BID", emp1.getBadgeId());
        Employee emp2 = Employee.builder().badgeId("BID2").firstName("F2").lastName("L2").role("HR").status("ACTIVE").deleted(false).createdAt(now).updatedAt(now).build();
        assertEquals("BID2", emp2.getBadgeId());
    }
}
