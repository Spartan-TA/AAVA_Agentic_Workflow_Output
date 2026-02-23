package SpringBootTestSuite;

import com.example.warehouse.dto.EmployeeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDtoTest {
    private Validator validator;
    private EmployeeDto dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        dto = new EmployeeDto();
        dto.setName("Alice Smith");
        dto.setBadgeId("ABCD1234");
        dto.setRole("Worker");
        dto.setDepartment("Logistics");
        dto.setShiftGroup("A");
        dto.setHireDate(LocalDate.now().minusDays(1));
        dto.setStatus("ACTIVE");
        dto.setEmail("alice.smith@example.com");
        dto.setPhone("+12345678901");
    }

    @Test
    void testValidDto_PassesValidation() {
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankName_FailsValidation() {
        dto.setName("");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testNameTooShort_FailsValidation() {
        dto.setName("A");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testNameTooLong_FailsValidation() {
        dto.setName("A".repeat(101));
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testBlankBadgeId_FailsValidation() {
        dto.setBadgeId("");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testInvalidBadgeIdFormat_FailsValidation() {
        dto.setBadgeId("bad#id");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testBadgeIdTooShort_FailsValidation() {
        dto.setBadgeId("ABC");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testBadgeIdTooLong_FailsValidation() {
        dto.setBadgeId("A".repeat(21));
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testBlankStatus_FailsValidation() {
        dto.setStatus("");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    void testInvalidEmail_FailsValidation() {
        dto.setEmail("not-an-email");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testInvalidPhone_FailsValidation() {
        dto.setPhone("123abc");
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void testNullHireDate_FailsValidation() {
        dto.setHireDate(null);
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    void testFutureHireDate_FailsValidation() {
        dto.setHireDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    void testNullName_FailsValidation() {
        dto.setName(null);
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testNullBadgeId_FailsValidation() {
        dto.setBadgeId(null);
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testNullStatus_FailsValidation() {
        dto.setStatus(null);
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
}
