package com.wems.employee.dto;

import com.wems.employee.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmployeeDTOTest {
    private EmployeeDTO employeeDTO;
    private Validator validator;

    @BeforeEach
    void setUp() {
        employeeDTO = new EmployeeDTO();
        employeeDTO.setName("John Doe");
        employeeDTO.setEmail("john.doe@example.com");
        employeeDTO.setBadgeId("BADGE123");
        employeeDTO.setRole("WORKER");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validEmployeeDTO_NoViolations() {
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmail_Violation() {
        employeeDTO.setEmail("invalid-email");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void blankFields_Violations() {
        employeeDTO.setName("");
        employeeDTO.setEmail("");
        employeeDTO.setBadgeId("");
        employeeDTO.setRole("");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    void boundaryValues_MaxLength() {
        employeeDTO.setName("a".repeat(100));
        employeeDTO.setBadgeId("b".repeat(20));
        employeeDTO.setRole("c".repeat(50));
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void boundaryValues_ExceedMaxLength_Violations() {
        employeeDTO.setName("a".repeat(101));
        employeeDTO.setBadgeId("b".repeat(21));
        employeeDTO.setRole("c".repeat(51));
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    void toEntity_Conversion() {
        Employee entity = employeeDTO.toEntity();
        assertNotNull(entity);
        assertEquals(employeeDTO.getName(), entity.getName());
        assertEquals(employeeDTO.getEmail(), entity.getEmail());
        assertEquals(employeeDTO.getBadgeId(), entity.getBadgeId());
        assertEquals(employeeDTO.getRole(), entity.getRole());
        assertTrue(entity.isActive());
    }

    @Test
    void toEntity_NullFields() {
        EmployeeDTO nullDTO = new EmployeeDTO();
        Employee entity = nullDTO.toEntity();
        assertNotNull(entity);
        assertNull(entity.getName());
        assertNull(entity.getEmail());
        assertNull(entity.getBadgeId());
        assertNull(entity.getRole());
        assertTrue(entity.isActive());
    }
}
