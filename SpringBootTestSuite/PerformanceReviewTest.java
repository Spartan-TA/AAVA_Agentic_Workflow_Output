import org.junit.jupiter.api.*;
import javax.validation.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class PerformanceReviewTest {
    private Validator validator;
    private PerformanceReview review;
    private Employee employee;
    private Employee reviewer;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        employee = new Employee();
        employee.setId(1L);
        reviewer = new Employee();
        reviewer.setId(2L);
        review = new PerformanceReview();
        review.setPeriod("2024-Q2");
        review.setGoals("Increase productivity");
        review.setRatings(4);
        review.setComments("Good performance");
        review.setStatus("DRAFT");
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewDate(LocalDate.now());
    }

    @AfterEach
    public void tearDown() {
        validator = null;
        review = null;
        employee = null;
        reviewer = null;
    }

    @Test
    public void testValidReview_ShouldPassValidation() {
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNullPeriod_ShouldFailValidation() {
        review.setPeriod(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyPeriod_ShouldFailValidation() {
        review.setPeriod("");
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullGoals_ShouldFailValidation() {
        review.setGoals(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullRatings_ShouldFailValidation() {
        review.setRatings(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNegativeRatings_ShouldFailValidation() {
        review.setRatings(-1);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testRatingsOutOfRange_ShouldFailValidation() {
        review.setRatings(6);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullStatus_ShouldFailValidation() {
        review.setStatus(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidStatus_ShouldFailValidation() {
        review.setStatus("FINALIZED");
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullEmployee_ShouldFailValidation() {
        review.setEmployee(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullReviewer_ShouldFailValidation() {
        review.setReviewer(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testSameEmployeeAndReviewer_ShouldFailBusinessLogic() {
        review.setReviewer(employee);
        assertFalse(review.isReviewerValid());
    }

    @Test
    public void testNullReviewDate_ShouldFailValidation() {
        review.setReviewDate(null);
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testFutureReviewDate_ShouldFailValidation() {
        review.setReviewDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<PerformanceReview>> violations = validator.validate(review);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAcknowledgementWorkflow_ShouldBeValid() {
        review.setStatus("ACKNOWLEDGED");
        assertEquals("ACKNOWLEDGED", review.getStatus());
    }
}
