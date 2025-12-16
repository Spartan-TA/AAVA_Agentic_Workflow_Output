import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PerformanceReviewServiceTest {
    @Mock
    private PerformanceReviewRepository performanceReviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReview_Valid() {
        Employee employee = new Employee();
        employee.setId(1L);
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewPeriod("Q1 2024");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        PerformanceReview result = performanceReviewService.createReview(1L, review);
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
    }

    @Test
    void testCreateReview_EmployeeNotFound() {
        PerformanceReview review = new PerformanceReview();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> performanceReviewService.createReview(1L, review));
    }

    @Test
    void testAcknowledgeReview_Supervisor() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setStatus("DRAFT");
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        PerformanceReview result = performanceReviewService.acknowledgeBySupervisor(1L);
        assertNotNull(result);
        assertTrue(result.getSupervisorAcknowledged());
    }

    @Test
    void testAcknowledgeReview_Employee() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setSupervisorAcknowledged(true);
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        PerformanceReview result = performanceReviewService.acknowledgeByEmployee(1L);
        assertNotNull(result);
        assertTrue(result.getEmployeeAcknowledged());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void testFinalizeReview_Valid() {
        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setSupervisorAcknowledged(true);
        review.setEmployeeAcknowledged(true);
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        PerformanceReview result = performanceReviewService.finalizeReview(1L);
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
    }
}