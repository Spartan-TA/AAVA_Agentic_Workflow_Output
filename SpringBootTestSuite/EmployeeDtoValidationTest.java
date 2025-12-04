import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeUpdateDto;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeDtoValidationTest {

    private final Validator validator = new LocalValidatorFactoryBean();

    @Test
    void testValidEmployeeCreateDto() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setBadgeId("12345");
        dto.setName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("123-456-7890");
        dto.setRole("Manager");
        dto.setDepartment("Operations");
        dto.setHireDate(LocalDate.of(2020, 1, 1));
        dto.setStatus("Active");

        Set<ConstraintViolation<EmployeeCreateDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidEmailInEmployeeCreateDto() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setBadgeId("12345");
        dto.setName("John Doe");
        dto.setEmail("invalid-email");
        dto.setPhone("123-456-7890");
        dto.setRole("Manager");
        dto.setDepartment("Operations");
        dto.setHireDate(LocalDate.of(2020, 1, 1));
        dto.setStatus("Active");

        Set<ConstraintViolation<EmployeeCreateDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void testPastHireDateInEmployeeCreateDto() {
        EmployeeCreateDto dto = new EmployeeCreateDto();
        dto.setBadgeId("12345");
        dto.setName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("123-456-7890");
        dto.setRole("Manager");
        dto.setDepartment("Operations");
        dto.setHireDate(LocalDate.of(2025, 1, 1));
        dto.setStatus("Active");

        Set<ConstraintViolation<EmployeeCreateDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void testValidEmployeeUpdateDto() {
        EmployeeUpdateDto dto = new EmployeeUpdateDto();
        dto.setName("John Updated");
        dto.setEmail("john.updated@example.com");
        dto.setPhone("123-456-7890");

        Set<ConstraintViolation<EmployeeUpdateDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidPhoneInEmployeeUpdateDto() {
        EmployeeUpdateDto dto = new EmployeeUpdateDto();
        dto.setName("John Updated");
        dto.setEmail("john.updated@example.com");
        dto.setPhone("invalid-phone");

        Set<ConstraintViolation<EmployeeUpdateDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }
}