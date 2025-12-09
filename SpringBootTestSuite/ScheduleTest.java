import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleTest {
    private Validator validator;
    private Schedule schedule;
    private Employee employee;
    private Shift shift;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
        employee.setId(1L);
        shift = new Shift();
        shift.setId(1L);
        schedule = new Schedule();
        schedule.setDate(LocalDate.now());
        schedule.setStatus("ACTIVE");
        schedule.setEmployee(employee);
        schedule.setShift(shift);
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        schedule = null;
        employee = null;
        shift = null;
    }

    @Test
    public void testValidSchedule_ShouldPassValidation() {
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullDate_ShouldFailValidation() {
        schedule.setDate(null);
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testPastDate_ShouldFailValidation() {
        schedule.setDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFutureDate_ShouldPassValidation() {
        schedule.setDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullStatus_ShouldFailValidation() {
        schedule.setStatus(null);
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidStatus_ShouldFailValidation() {
        schedule.setStatus("INVALID");
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEmployee_ShouldFailValidation() {
        schedule.setEmployee(null);
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullShift_ShouldFailValidation() {
        schedule.setShift(null);
        Set<ConstraintViolation<Schedule>> violations = validator.validate(schedule);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testRelationshipIntegrity_ShouldBeValid() {
        assertEquals(employee, schedule.getEmployee());
        assertEquals(shift, schedule.getShift());
    }
}
