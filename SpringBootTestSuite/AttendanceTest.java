import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class AttendanceTest {
    private Validator validator;
    private Attendance attendance;
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
        attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setShift(shift);
        attendance.setClockIn(LocalDateTime.now().minusHours(8));
        attendance.setClockOut(LocalDateTime.now());
        attendance.setHoursWorked(8.0);
        attendance.setLocation("Main Gate");
        attendance.setDeviceInfo("Device123");
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        attendance = null;
        employee = null;
        shift = null;
    }

    @Test
    public void testValidAttendance_ShouldPassValidation() {
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullClockIn_ShouldFailValidation() {
        attendance.setClockIn(null);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullClockOut_ShouldFailValidation() {
        attendance.setClockOut(null);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFutureClockIn_ShouldFailValidation() {
        attendance.setClockIn(LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNegativeHoursWorked_ShouldFailValidation() {
        attendance.setHoursWorked(-2.0);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testHoursCalculationLogic_ShouldBeCorrect() {
        attendance.setClockIn(LocalDateTime.of(2024, 6, 1, 8, 0));
        attendance.setClockOut(LocalDateTime.of(2024, 6, 1, 16, 0));
        attendance.calculateHoursWorked();
        assertEquals(8.0, attendance.getHoursWorked());
    }

    @Test
    public void testNullLocation_ShouldFailValidation() {
        attendance.setLocation(null);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyDeviceInfo_ShouldFailValidation() {
        attendance.setDeviceInfo("");
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEmployee_ShouldFailValidation() {
        attendance.setEmployee(null);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullShift_ShouldFailValidation() {
        attendance.setShift(null);
        Set<ConstraintViolation<Attendance>> violations = validator.validate(attendance);
        assertFalse(violations.isEmpty());
    }
}
