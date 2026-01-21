package SpringBootTestSuite;

import com.example.warehouse.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeDTOValidationTest {
    private Validator validator;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employeeDTO = EmployeeDTO.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusYears(1))
                .terminationDate(null)
                .status("ACTIVE")
                .build();
    }

    @Test
    void validEmployeeDTO_NoViolations() {
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankName_Violation() {
        employeeDTO.setName("");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void nullBadgeId_Violation() {
        employeeDTO.setBadgeId(null);
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void invalidEmail_Violation() {
        employeeDTO.setEmail("not-an-email");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void invalidPhonePattern_Violation() {
        employeeDTO.setPhone("abc123");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void invalidRolePattern_Violation() {
        employeeDTO.setRole("INVALID_ROLE");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    void nullHireDate_Violation() {
        employeeDTO.setHireDate(null);
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    void futureHireDate_Violation() {
        employeeDTO.setHireDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    void pastOrPresentTerminationDate_Success() {
        employeeDTO.setTerminationDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void futureTerminationDate_Violation() {
        employeeDTO.setTerminationDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("terminationDate")));
    }

    @Test
    void minMaxLengthName() {
        employeeDTO.setName("A");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
        employeeDTO.setName("A".repeat(255));
        violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankDepartment_NoViolation() {
        employeeDTO.setDepartment("");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankShiftGroup_NoViolation() {
        employeeDTO.setShiftGroup("");
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        assertTrue(violations.isEmpty());
    }
}
