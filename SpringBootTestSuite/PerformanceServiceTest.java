package SpringBootTestSuite;

import com.example.warehouse.performance.PerformanceReview;
import com.example.warehouse.performance.ReviewTemplate;
import com.example.warehouse.performance.Goal;
import com.example.warehouse.performance.PerformanceService;
import com.example.warehouse.performance.PerformanceRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PerformanceServiceTest {
    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private PerformanceService performanceService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void createReviewTemplate_ValidInput_ReturnsReviewTemplate() {
        ReviewTemplate template = new ReviewTemplate();
        template.setName("Annual Review");
        when(performanceRepository.saveReviewTemplate(any())).thenReturn(template);
        ReviewTemplate result = performanceService.createReviewTemplate(template);
        assertNotNull(result);
        assertEquals("Annual Review", result.getName());
    }

    @Test
    public void createReviewTemplate_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> performanceService.createReviewTemplate(null));
    }

    @Test
    public void createPerformanceReview_ValidInput_ReturnsPerformanceReview() {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(1L);
        when(performanceRepository.savePerformanceReview(any())).thenReturn(review);
        PerformanceReview result = performanceService.createPerformanceReview(review);
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void createPerformanceReview_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> performanceService.createPerformanceReview(null));
    }

    @Test
    public void assignGoal_ValidInput_ReturnsGoal() {
        Goal goal = new Goal();
        goal.setEmployeeId(1L);
        goal.setDescription("Increase productivity");
        when(performanceRepository.saveGoal(any())).thenReturn(goal);
        Goal result = performanceService.assignGoal(goal);
        assertNotNull(result);
        assertEquals("Increase productivity", result.getDescription());
    }

    @Test
    public void assignGoal_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> performanceService.assignGoal(null));
    }

    @Test
    public void getPerformanceReviewsByEmployeeId_ValidId_ReturnsList() {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(1L);
        when(performanceRepository.findPerformanceReviewsByEmployeeId(1L)).thenReturn(Collections.singletonList(review));
        List<PerformanceReview> result = performanceService.getPerformanceReviewsByEmployeeId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getPerformanceReviewsByEmployeeId_NoReviews_ReturnsEmptyList() {
        when(performanceRepository.findPerformanceReviewsByEmployeeId(1L)).thenReturn(Collections.emptyList());
        List<PerformanceReview> result = performanceService.getPerformanceReviewsByEmployeeId(1L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
