package com.example.warehouse_employee_mgmt_epics;

import org.junit.jupiter.api.*;
import javax.validation.*;
import java.util.Set;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeEntityTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidEmployeeEntity() {
        Employee employee = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty(), "Valid employee should have no violations");
    }

    @Test
    public void testBlankFirstName() {
        Employee employee = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("")
                .lastName("Doe")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Blank first name should trigger violation");
    }

    @Test
    public void testInvalidEmailFormat() {
        Employee employee = Employee.builder()
                .badgeId("12345")
                .email("invalid-email")
                .firstName("John")
                .lastName("Doe")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")), "Invalid email should trigger violation");
    }

    @Test
    public void testNullFields() {
        Employee employee = Employee.builder()
                .badgeId(null)
                .email(null)
                .firstName(null)
                .lastName(null)
                .department(null)
                .role(null)
                .status(null)
                .isActive(null)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty(), "Null fields should trigger violations");
    }

    @Test
    public void testPrePersistSetsCreatedAt() {
        Employee employee = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        employee.prePersist();
        assertNotNull(employee.getCreatedAt(), "createdAt should be set on prePersist");
    }

    @Test
    public void testPreUpdateSetsUpdatedAt() {
        Employee employee = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .department("Logistics")
                .role("WORKER")
                .status("ACTIVE")
                .isActive(true)
                .build();

        employee.preUpdate();
        assertNotNull(employee.getUpdatedAt(), "updatedAt should be set on preUpdate");
    }

    @Test
    public void testEqualsAndHashCode() {
        Employee emp1 = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Employee emp2 = Employee.builder()
                .badgeId("12345")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals(emp1, emp2, "Employees with same fields should be equal");
        assertEquals(emp1.hashCode(), emp2.hashCode(), "Hash codes should match for equal employees");
    }

    @Test
    public void testBuilderPattern() {
        Employee employee = Employee.builder()
                .badgeId("99999")
                .email("builder@example.com")
                .firstName("Builder")
                .lastName("Pattern")
                .build();

        assertEquals("99999", employee.getBadgeId());
        assertEquals("builder@example.com", employee.getEmail());
        assertEquals("Builder", employee.getFirstName());
        assertEquals("Pattern", employee.getLastName());
    }
}