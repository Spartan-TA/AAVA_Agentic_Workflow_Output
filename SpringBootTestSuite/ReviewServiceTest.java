package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReviewService reviewService;

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
    void testCreateReview_Valid_Success() {
        Review review = new Review(1L, 1L, "Quarterly", "Good performance", 4, "PENDING");
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = reviewService.createReview(review);
        assertNotNull(result);
        assertEquals("Quarterly", result.getType());
    }

    @Test
    void testCreateReview_NullType_ThrowsException() {
        Review review = new Review(1L, 1L, null, "Good performance", 4, "PENDING");
        assertThrows(InvalidReviewException.class, () -> reviewService.createReview(review));
    }

    @Test
    void testAcknowledgeReview_Valid_Success() {
        Review review = new Review(1L, 1L, "Quarterly", "Good performance", 4, "PENDING");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = reviewService.acknowledgeReview(1L);
        assertEquals("ACKNOWLEDGED", result.getStatus());
    }

    @Test
    void testAcknowledgeReview_InvalidId_ThrowsException() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.acknowledgeReview(99L));
    }

    @Test
    void testGetReviewsByEmployee_Valid_Success() {
        List<Review> reviews = Arrays.asList(
            new Review(1L, 1L, "Quarterly", "Good performance", 4, "ACKNOWLEDGED"),
            new Review(2L, 1L, "Annual", "Excellent", 5, "ACKNOWLEDGED")
        );
        when(reviewRepository.findByEmployeeId(1L)).thenReturn(reviews);
        List<Review> result = reviewService.getReviewsByEmployee(1L);
        assertEquals(2, result.size());
    }

    @Test
    void testExportReviewToPDF_Valid_Success() {
        Review review = new Review(1L, 1L, "Annual", "Excellent", 5, "ACKNOWLEDGED");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        PDFDocument pdf = reviewService.exportReviewToPDF(1L);
        assertNotNull(pdf);
    }

    // Integration scenario: Immutable history after sign-off
    @Test
    void testImmutableHistoryAfterSignOff_Success() {
        Review review = new Review(1L, 1L, "Annual", "Excellent", 5, "ACKNOWLEDGED");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertThrows(ImmutableReviewException.class, () -> reviewService.updateReview(1L, "Updated comment", 5));
    }
}
