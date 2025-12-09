import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class CertificationTest {
    private Validator validator;
    private Certification certification;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
        employee.setId(1L);
        certification = new Certification();
        certification.setType("Forklift License");
        certification.setIssueDate(LocalDate.now().minusYears(1));
        certification.setExpiryDate(LocalDate.now().plusYears(1));
        certification.setProofDocumentUrl("http://example.com/doc.pdf");
        certification.setEmployee(employee);
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        certification = null;
        employee = null;
    }

    @Test
    public void testValidCertification_ShouldPassValidation() {
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullType_ShouldFailValidation() {
        certification.setType(null);
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyType_ShouldFailValidation() {
        certification.setType("");
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullIssueDate_ShouldFailValidation() {
        certification.setIssueDate(null);
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullExpiryDate_ShouldFailValidation() {
        certification.setExpiryDate(null);
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testExpiryDateBeforeIssueDate_ShouldFailBusinessLogic() {
        certification.setIssueDate(LocalDate.now());
        certification.setExpiryDate(LocalDate.now().minusDays(1));
        assertFalse(certification.isValidDateRange());
    }

    @Test
    public void testFutureIssueDate_ShouldFailValidation() {
        certification.setIssueDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testExpiredCertificationCheck_ShouldBeExpired() {
        certification.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(certification.isExpired());
    }

    @Test
    public void testCertificationValidityCheck_ShouldBeValid() {
        certification.setExpiryDate(LocalDate.now().plusDays(10));
        assertTrue(certification.isValid());
    }

    @Test
    public void testNullEmployee_ShouldFailValidation() {
        certification.setEmployee(null);
        Set<ConstraintViolation<Certification>> violations = validator.validate(certification);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDocumentUrlValidation_ShouldBeValid() {
        certification.setProofDocumentUrl("http://example.com/doc.pdf");
        assertTrue(certification.isDocumentUrlValid());
    }
}
