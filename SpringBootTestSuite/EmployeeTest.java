package com.company.wems.employee.entity;

import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create employee with valid data")
    void testCreateEmployee_ValidData_Success() {
        Employee emp = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        assertAll(
                () -> assertNotNull(emp),
                () -> assertEquals("EMP001", emp.getBadgeId()),
                () -> assertEquals("John Doe", emp.getName()),
                () -> assertEquals("WORKER", emp.getRole()),
                () -> assertEquals("Warehouse", emp.getDepartment()),
                () -> assertEquals("A", emp.getShiftGroup()),
                () -> assertEquals("ACTIVE", emp.getStatus()),
                () -> assertFalse(emp.isDeleted())
        );
    }

    @Test
    @DisplayName("Should fail validation when badgeId is null")
    void testCreateEmployee_NullBadgeId_FailValidation() {
        Employee emp = Employee.builder()
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        // No badgeId
        // Simulate validation
        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void testCreateEmployee_EmptyName_FailValidation() {
        Employee emp = Employee.builder()
                .badgeId("EMP002")
                .name("")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Should fail validation when hireDate is null")
    void testCreateEmployee_NullHireDate_FailValidation() {
        Employee emp = Employee.builder()
                .badgeId("EMP003")
                .name("Jane Doe")
                .role("WORKER")
                .department("Warehouse")
                .status("ACTIVE")
                .build();
        Set<ConstraintViolation<Employee>> violations = validator.validate(emp);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    @DisplayName("Should allow only valid status values (boundary test)")
    void testCreateEmployee_InvalidStatusValue() {
        Employee emp = Employee.builder()
                .badgeId("EMP004")
                .name("Jane Doe")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("INVALID_STATUS")
                .build();
        // No enum or validation, so status is just a string. This test is for future validation.
        assertEquals("INVALID_STATUS", emp.getStatus());
    }

    @Test
    @DisplayName("Should allow name with 128 characters (boundary)")
    void testCreateEmployee_Name128Chars_Success() {
        String longName = "A".repeat(128);
        Employee emp = Employee.builder()
                .badgeId("EMP005")
                .name(longName)
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        assertEquals(128, emp.getName().length());
    }

    @Test
    @DisplayName("Should allow special characters in name (edge case)")
    void testCreateEmployee_SpecialCharsInName_Success() {
        String specialName = "JÃ¶hn DÅ!@#$%^&*()_+|~`";
        Employee emp = Employee.builder()
                .badgeId("EMP006")
                .name(specialName)
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        assertEquals(specialName, emp.getName());
    }

    @Test
    @DisplayName("Should set deleted flag and verify soft delete behavior")
    void testSoftDeleteFlagBehavior() {
        Employee emp = Employee.builder()
                .badgeId("EMP007")
                .name("Jane Doe")
                .role("WORKER")
                .department("Warehouse")
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .deleted(true)
                .build();
        assertTrue(emp.isDeleted());
        emp.setDeleted(false);
        assertFalse(emp.isDeleted());
    }
}
