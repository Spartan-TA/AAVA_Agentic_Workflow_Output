import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SafetyIncidentTest {
    private Validator validator;
    private SafetyIncident incident;
    private Employee reporter;
    private Employee involved;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        reporter = new Employee();
        reporter.setId(1L);
        involved = new Employee();
        involved.setId(2L);
        incident = new SafetyIncident();
        incident.setSeverity("HIGH");
        incident.setLocation("Warehouse A");
        incident.setDescription("Forklift collision");
        incident.setStatus("OPEN");
        incident.setIncidentDate(LocalDateTime.now().minusHours(1));
        incident.setReporter(reporter);
        incident.setInvolvedEmployees(new HashSet<>(Arrays.asList(involved)));
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        incident = null;
        reporter = null;
        involved = null;
    }

    @Test
    public void testValidIncident_ShouldPassValidation() {
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullSeverity_ShouldFailValidation() {
        incident.setSeverity(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidSeverity_ShouldFailValidation() {
        incident.setSeverity("EXTREME");
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullLocation_ShouldFailValidation() {
        incident.setLocation(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyLocation_ShouldFailValidation() {
        incident.setLocation("");
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullDescription_ShouldFailValidation() {
        incident.setDescription(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyDescription_ShouldFailValidation() {
        incident.setDescription("");
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullStatus_ShouldFailValidation() {
        incident.setStatus(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidStatus_ShouldFailValidation() {
        incident.setStatus("CLOSED");
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullIncidentDate_ShouldFailValidation() {
        incident.setIncidentDate(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFutureIncidentDate_ShouldFailValidation() {
        incident.setIncidentDate(LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullReporter_ShouldFailValidation() {
        incident.setReporter(null);
        Set<ConstraintViolation<SafetyIncident>> violations = validator.validate(incident);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvolvedEmployeesValidation_ShouldBeValid() {
        assertTrue(incident.getInvolvedEmployees().contains(involved));
    }
}
