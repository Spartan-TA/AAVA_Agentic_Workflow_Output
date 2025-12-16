import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class PerformanceServiceTest {
    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @InjectMocks
    private PerformanceService performanceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateReview_ValidInput() {
        PerformanceReview review = new PerformanceReview("EMP123", "Q1", "Supervisor", "Pending");
        when(performanceReviewRepository.save(any())).thenReturn(review);
        PerformanceReview result = performanceService.createReview(review);
        assertEquals("EMP123", result.getEmployeeId());
        assertEquals("Q1", result.getCycle());
    }

    @Test
    public void testCreateReview_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> performanceService.createReview(null));
    }

    @Test
    public void testSubmitReview_Valid() {
        PerformanceReview review = new PerformanceReview("EMP123", "Q1", "Supervisor", "Submitted");
        when(performanceReviewRepository.save(any())).thenReturn(review);
        PerformanceReview submitted = performanceService.submitReview(review);
        assertEquals("Submitted", submitted.getStatus());
    }

    @Test
    public void testAcknowledgeReview_Valid() {
        PerformanceReview review = new PerformanceReview("EMP123", "Q1", "Supervisor", "Submitted");
        when(performanceReviewRepository.save(any())).thenReturn(review);
        PerformanceReview acknowledged = performanceService.acknowledgeReview(review);
        assertEquals("Acknowledged", acknowledged.getStatus());
    }

    @Test
    public void testExportReviewPDF_Valid() {
        PerformanceReview review = new PerformanceReview("EMP123", "Q1", "Supervisor", "Acknowledged");
        when(performanceReviewRepository.findById(anyLong())).thenReturn(java.util.Optional.of(review));
        byte[] pdf = performanceService.exportReviewPDF(1L);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
