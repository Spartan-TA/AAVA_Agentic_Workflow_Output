package com.example.warehouse.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class EmployeeDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidation_NullName_FailsValidation() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("BADGE123");
        dto.setEmail("john@example.com");

        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void testValidation_EmptyBadgeId_FailsValidation() {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("John Doe");
        dto.setBadgeId("");
        dto.setEmail("john@example.com");

        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("badgeId"));
    }

    @Test
    void testValidation_InvalidEmail_FailsValidation() {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("John Doe");
        dto.setBadgeId("BADGE123");
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void testValidation_ValidDto_PassesValidation() {
        EmployeeDto dto = new EmployeeDto();
        dto.setName("John Doe");
        dto.setBadgeId("BADGE123");
        dto.setEmail("john@example.com");

        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}