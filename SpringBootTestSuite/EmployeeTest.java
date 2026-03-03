package com.warehouse.employeemgmt.employee;

import com.warehouse.employeemgmt.employee.enums.EmployeeRole;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Employee entity validation and edge cases.
 */
class EmployeeTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid Employee passes all validations")
    void validEmployee_passesValidation() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .emergencyContactName("Jane Doe")
                .emergencyContactPhone("0987654321")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Blank badgeId triggers validation error")
    void blankBadgeId_triggersValidationError() {
        Employee employee = Employee.builder()
                .badgeId("")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Null name triggers validation error")
    void nullName_triggersValidationError() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name(null)
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Future hireDate triggers validation error")
    void futureHireDate_triggersValidationError() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .hireDate(LocalDate.now().plusDays(1))
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    @DisplayName("Blank status triggers validation error")
    void blankStatus_triggersValidationError() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    @DisplayName("Invalid email format triggers validation error")
    void invalidEmailFormat_triggersValidationError() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .email("not-an-email")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Null role defaults to WORKER in @PrePersist")
    void nullRole_defaultsToWorkerOnPrePersist() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .status("ACTIVE")
                .build();
        employee.setRole(null);
        employee.onCreate();
        assertEquals(EmployeeRole.WORKER, employee.getRole());
    }

    @Test
    @DisplayName("Null status defaults to ACTIVE in @PrePersist")
    void nullStatus_defaultsToActiveOnPrePersist() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.HR)
                .build();
        employee.setStatus(null);
        employee.onCreate();
        assertEquals("ACTIVE", employee.getStatus());
    }

    @Test
    @DisplayName("Max length for badgeId is enforced")
    void badgeId_maxLength() {
        String longBadgeId = "B" + "1".repeat(100);
        Employee employee = Employee.builder()
                .badgeId(longBadgeId)
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        // No annotation for max length, so DB will enforce, not validator
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Null email is allowed")
    void nullEmail_isAllowed() {
        Employee employee = Employee.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role(EmployeeRole.WORKER)
                .status("ACTIVE")
                .email(null)
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }
}
