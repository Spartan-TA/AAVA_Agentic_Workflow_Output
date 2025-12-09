import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class AuditLogTest {
    private Validator validator;
    private AuditLog log;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        log = new AuditLog();
        log.setEntityType("Employee");
        log.setEntityId(1L);
        log.setAction("CREATE");
        log.setActor("admin");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeValue(null);
        log.setAfterValue("{"name":"John"}");
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        log = null;
    }

    @Test
    public void testValidAuditLog_ShouldPassValidation() {
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullEntityType_ShouldFailValidation() {
        log.setEntityType(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyEntityType_ShouldFailValidation() {
        log.setEntityType("");
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEntityId_ShouldFailValidation() {
        log.setEntityId(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNegativeEntityId_ShouldFailValidation() {
        log.setEntityId(-1L);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullAction_ShouldFailValidation() {
        log.setAction(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidAction_ShouldFailValidation() {
        log.setAction("ARCHIVE");
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullActor_ShouldFailValidation() {
        log.setActor(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyActor_ShouldFailValidation() {
        log.setActor("");
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullTimestamp_ShouldFailValidation() {
        log.setTimestamp(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFutureTimestamp_ShouldFailValidation() {
        log.setTimestamp(LocalDateTime.now().plusDays(1));
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullBeforeValueForUpdate_ShouldBeAllowed() {
        log.setAction("UPDATE");
        log.setBeforeValue(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullAfterValueForCreateUpdate_ShouldFailValidation() {
        log.setAction("CREATE");
        log.setAfterValue(null);
        Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testImmutabilityCheck_ShouldBeImmutable() {
        assertTrue(log.isImmutable());
    }
}
