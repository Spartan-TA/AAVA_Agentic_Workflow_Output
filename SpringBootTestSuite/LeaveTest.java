import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class LeaveTest {
    private Validator validator;
    private Leave leave;
    private Employee employee;
    private Employee approver;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
        employee.setId(1L);
        approver = new Employee();
        approver.setId(2L);
        leave = new Leave();
        leave.setType("PTO");
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(5));
        leave.setStatus("PENDING");
        leave.setReason("Vacation");
        leave.setEmployee(employee);
        leave.setApprover(approver);
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        leave = null;
        employee = null;
        approver = null;
    }

    @Test
    public void testValidLeave_ShouldPassValidation() {
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullType_ShouldFailValidation() {
        leave.setType(null);
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidType_ShouldFailValidation() {
        leave.setType("HOLIDAY");
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullStartDate_ShouldFailValidation() {
        leave.setStartDate(null);
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEndDate_ShouldFailValidation() {
        leave.setEndDate(null);
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEndDateBeforeStartDate_ShouldFailBusinessLogic() {
        leave.setStartDate(LocalDate.now().plusDays(5));
        leave.setEndDate(LocalDate.now().plusDays(1));
        assertFalse(leave.isValidDateRange());
    }

    @Test
    public void testNullStatus_ShouldFailValidation() {
        leave.setStatus(null);
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidStatus_ShouldFailValidation() {
        leave.setStatus("ON_HOLD");
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEmployee_ShouldFailValidation() {
        leave.setEmployee(null);
        Set<ConstraintViolation<Leave>> violations = validator.validate(leave);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testLeaveDurationCalculation_ShouldBeCorrect() {
        leave.setStartDate(LocalDate.of(2024, 6, 1));
        leave.setEndDate(LocalDate.of(2024, 6, 5));
        assertEquals(5, leave.getDurationDays());
    }

    @Test
    public void testApprovalWorkflow_ShouldBeValid() {
        leave.setStatus("APPROVED");
        leave.setApprover(approver);
        assertEquals("APPROVED", leave.getStatus());
        assertEquals(approver, leave.getApprover());
    }
}
