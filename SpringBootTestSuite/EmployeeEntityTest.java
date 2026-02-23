package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Assuming Employee is in com.example.warehouse.entity
import com.example.warehouse.entity.Employee;

class EmployeeEntityTest {
    private Employee employee;
    private Validator validator;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Alice Smith");
        employee.setBadgeId("ABCD1234");
        employee.setRole("Worker");
        employee.setDepartment("Logistics");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.now().minusDays(1));
        employee.setStatus("ACTIVE");
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.now().minusDays(1));
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setEmail("alice.smith@example.com");
        employee.setPhone("+12345678901");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEntityCreation_ValidFields_NoViolations() {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testSoftDelete_SetsDeletedFlagAndStatus() {
        employee.softDelete();
        assertTrue(employee.isDeleted());
        assertEquals("TERMINATED", employee.getStatus());
    }

    @Test
    void testIsActive_StatusActive_ReturnsTrue() {
        employee.setStatus("ACTIVE");
        assertTrue(employee.isActive());
    }

    @Test
    void testIsActive_StatusSuspended_ReturnsFalse() {
        employee.setStatus("SUSPENDED");
        assertFalse(employee.isActive());
    }

    @Test
    void testIsActive_StatusOnLeave_ReturnsFalse() {
        employee.setStatus("ON_LEAVE");
        assertFalse(employee.isActive());
    }

    @Test
    void testIsActive_StatusTerminated_ReturnsFalse() {
        employee.setStatus("TERMINATED");
        assertFalse(employee.isActive());
    }

    @Test
    void testOnCreate_SetsCreatedAtAndUpdatedAt() {
        Employee newEmployee = new Employee();
        newEmployee.onCreate();
        assertNotNull(newEmployee.getCreatedAt());
        assertNotNull(newEmployee.getUpdatedAt());
        assertEquals(newEmployee.getCreatedAt(), newEmployee.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters_AllFields() {
        Employee e = new Employee();
        e.setId(2L);
        e.setName("Bob");
        e.setBadgeId("XYZ9876");
        e.setRole("Manager");
        e.setDepartment("Admin");
        e.setShiftGroup("B");
        e.setHireDate(LocalDate.now().minusYears(1));
        e.setStatus("ON_LEAVE");
        e.setDeleted(true);
        e.setCreatedAt(LocalDateTime.now().minusYears(1));
        e.setUpdatedAt(LocalDateTime.now());
        e.setEmail("bob@example.com");
        e.setPhone("+19876543210");

        assertEquals(2L, e.getId());
        assertEquals("Bob", e.getName());
        assertEquals("XYZ9876", e.getBadgeId());
        assertEquals("Manager", e.getRole());
        assertEquals("Admin", e.getDepartment());
        assertEquals("B", e.getShiftGroup());
        assertEquals("ON_LEAVE", e.getStatus());
        assertTrue(e.isDeleted());
        assertEquals("bob@example.com", e.getEmail());
        assertEquals("+19876543210", e.getPhone());
    }

    @Test
    void testEntityCreation_InvalidName_TooShort() {
        employee.setName("A");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testEntityCreation_InvalidName_TooLong() {
        employee.setName("A".repeat(101));
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testEntityCreation_InvalidBadgeId_Format() {
        employee.setBadgeId("bad#id");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testEntityCreation_InvalidEmail_Format() {
        employee.setEmail("not-an-email");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testEntityCreation_InvalidPhone_Format() {
        employee.setPhone("123abc");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void testEntityCreation_HireDateInFuture_FailsValidation() {
        employee.setHireDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }

    @Test
    void testEntityCreation_BlankName_FailsValidation() {
        employee.setName("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testEntityCreation_BlankBadgeId_FailsValidation() {
        employee.setBadgeId("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("badgeId")));
    }

    @Test
    void testEntityCreation_BlankStatus_FailsValidation() {
        employee.setStatus("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    void testEntityCreation_NullHireDate_FailsValidation() {
        employee.setHireDate(null);
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hireDate")));
    }
}
