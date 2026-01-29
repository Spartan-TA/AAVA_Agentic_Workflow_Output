package SpringBootTestSuite;

import com.warehouse.employee_mgmt.dto.EmployeeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDtoValidationTest {
    private Validator validator;
    private EmployeeDto validDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validDto = EmployeeDto.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .tenantId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("testValidDto_NoViolations")
    void testValidDto_NoViolations() {
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("testName_NotBlank_ValidationError")
    void testName_NotBlank_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().name("").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("testName_Size_TooLong_ValidationError")
    void testName_Size_TooLong_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().name("A".repeat(101)).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("testBadgeId_NotBlank_ValidationError")
    void testBadgeId_NotBlank_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().badgeId("").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("testBadgeId_Size_TooLong_ValidationError")
    void testBadgeId_Size_TooLong_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().badgeId("B".repeat(51)).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    @DisplayName("testRole_NotBlank_ValidationError")
    void testRole_NotBlank_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().role("").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("testRole_Pattern_Invalid_ValidationError")
    void testRole_Pattern_Invalid_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().role("INVALID").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("testDepartment_Size_TooLong_ValidationError")
    void testDepartment_Size_TooLong_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().department("D".repeat(51)).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("department")));
    }

    @Test
    @DisplayName("testShiftGroup_Size_TooLong_ValidationError")
    void testShiftGroup_Size_TooLong_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().shiftGroup("S".repeat(51)).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("shiftGroup")));
    }

    @Test
    @DisplayName("testHireDate_NotNull_ValidationError")
    void testHireDate_NotNull_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().hireDate(null).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    @DisplayName("testHireDate_PastOrPresent_FutureDate_ValidationError")
    void testHireDate_PastOrPresent_FutureDate_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().hireDate(LocalDate.now().plusDays(1)).build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    @DisplayName("testStatus_NotBlank_ValidationError")
    void testStatus_NotBlank_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().status("").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    @DisplayName("testStatus_Pattern_Invalid_ValidationError")
    void testStatus_Pattern_Invalid_ValidationError() {
        EmployeeDto dto = validDto.toBuilder().status("INVALID").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    // Boundary cases
    @Test
    @DisplayName("testName_Size_MinLength_Success")
    void testName_Size_MinLength_Success() {
        EmployeeDto dto = validDto.toBuilder().name("A").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("testBadgeId_Size_MinLength_Success")
    void testBadgeId_Size_MinLength_Success() {
        EmployeeDto dto = validDto.toBuilder().badgeId("B").build();
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
