package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class PerformanceReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TemplateRepository templateRepository;
    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testCreateReview_Valid() {
        when(templateRepository.existsById(1L)).thenReturn(true);
        Review review = new Review(2L, 1L, "2024Q1");
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = performanceReviewService.createReview(2L, 1L, "2024Q1");
        assertNotNull(result);
        assertEquals("2024Q1", result.getCycle());
    }

    @Test
    void testCreateReview_InvalidTemplate() {
        when(templateRepository.existsById(99L)).thenReturn(false);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            performanceReviewService.createReview(2L, 99L, "2024Q1"));
        assertEquals("Invalid template", ex.getMessage());
    }

    @Test
    void testCreateReview_NullCycle() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            performanceReviewService.createReview(2L, 1L, null));
        assertEquals("Cycle cannot be null", ex.getMessage());
    }

    @Test
    void testSetGoal_EmptyDescription() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            performanceReviewService.setGoal(1L, ""));
        assertEquals("Goal description cannot be empty", ex.getMessage());
    }

    @Test
    void testAcknowledgeReview_UnauthorizedUser() {
        Review review = new Review(1L, 2L, "2024Q1");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        Exception ex = assertThrows(SecurityException.class, () ->
            performanceReviewService.acknowledgeReview(1L, 99L));
        assertEquals("User not authorized to acknowledge", ex.getMessage());
    }

    @Test
    void testExportToPdf_Failure() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(new Review(1L, 2L, "2024Q1")));
        doThrow(new RuntimeException("PDF error")).when(reviewRepository).exportToPdf(1L);
        Exception ex = assertThrows(RuntimeException.class, () ->
            performanceReviewService.exportToPdf(1L));
        assertEquals("PDF error", ex.getMessage());
    }
}