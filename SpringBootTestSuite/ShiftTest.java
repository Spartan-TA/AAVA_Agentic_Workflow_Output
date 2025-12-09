import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class ShiftTest {
    private Validator validator;
    private Shift shift;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        shift = new Shift();
        shift.setName("Morning Shift");
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(16, 0));
        shift.setRecurrencePattern("WEEKLY");
        shift.setOvertimeThreshold(8);
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        shift = null;
    }

    @Test
    public void testValidShift_ShouldPassValidation() {
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullName_ShouldFailValidation() {
        shift.setName(null);
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyName_ShouldFailValidation() {
        shift.setName("");
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullStartTime_ShouldFailValidation() {
        shift.setStartTime(null);
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEndTime_ShouldFailValidation() {
        shift.setEndTime(null);
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEndTimeBeforeStartTime_ShouldFailBusinessLogic() {
        shift.setStartTime(LocalTime.of(16, 0));
        shift.setEndTime(LocalTime.of(8, 0));
        assertFalse(shift.isValidTimeRange());
    }

    @Test
    public void testInvalidRecurrencePattern_ShouldFailValidation() {
        shift.setRecurrencePattern("INVALID");
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNegativeOvertimeThreshold_ShouldFailValidation() {
        shift.setOvertimeThreshold(-1);
        Set<ConstraintViolation<Shift>> violations = validator.validate(shift);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testOvernightShift_ShouldBeValid() {
        shift.setStartTime(LocalTime.of(22, 0));
        shift.setEndTime(LocalTime.of(6, 0));
        assertTrue(shift.isOvernight());
    }
}
