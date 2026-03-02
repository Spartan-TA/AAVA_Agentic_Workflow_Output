package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class PerformanceReviewServiceImplTest {

    @Mock
    private PerformanceReviewRepository reviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private PerformanceReviewServiceImpl reviewService;

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
    @DisplayName("createReview - valid input - review created")
    void testCreateReview_ValidInput_ReviewCreated() {
        PerformanceReview review = new PerformanceReview(null, 1L, "2024-Q2", "Draft");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.save(any())).thenAnswer(i -> {
            PerformanceReview r = i.getArgument(0);
            r.setId(1L);
            return r;
        });
        PerformanceReview result = reviewService.createReview(review);
        assertNotNull(result.getId());
        assertEquals("Draft", result.getStatus());
    }

    @Test
    @DisplayName("createReview - employee not found - throws exception")
    void testCreateReview_EmployeeNotFound_ThrowsException() {
        PerformanceReview review = new PerformanceReview(null, 99L, "2024-Q2", "Draft");
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> reviewService.createReview(review));
    }

    @Test
    @DisplayName("submitReview - valid - status updated")
    void testSubmitReview_Valid_StatusUpdated() {
        PerformanceReview review = new PerformanceReview(1L, 1L, "2024-Q2", "Draft");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        reviewService.submitReview(1L);
        assertEquals("Submitted", review.getStatus());
    }

    @Test
    @DisplayName("submitReview - not found - throws exception")
    void testSubmitReview_NotFound_ThrowsException() {
        when(reviewRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.submitReview(2L));
    }

    @Test
    @DisplayName("acknowledgeReview - valid - status updated")
    void testAcknowledgeReview_Valid_StatusUpdated() {
        PerformanceReview review = new PerformanceReview(1L, 1L, "2024-Q2", "Submitted");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        reviewService.acknowledgeReview(1L);
        assertEquals("Acknowledged", review.getStatus());
    }

    @Test
    @DisplayName("acknowledgeReview - not found - throws exception")
    void testAcknowledgeReview_NotFound_ThrowsException() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.acknowledgeReview(3L));
    }

    @Test
    @DisplayName("getReviewsByEmployee - returns list")
    void testGetReviewsByEmployee_ReturnsList() {
        List<PerformanceReview> reviews = Arrays.asList(new PerformanceReview(1L, 1L, "2024-Q2", "Acknowledged"));
        when(reviewRepository.findByEmployeeId(1L)).thenReturn(reviews);
        List<PerformanceReview> result = reviewService.getReviewsByEmployee(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("exportReviewToPDF - valid - returns PDF bytes")
    void testExportReviewToPDF_Valid_ReturnsPDFBytes() {
        PerformanceReview review = new PerformanceReview(1L, 1L, "2024-Q2", "Acknowledged");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        byte[] pdf = reviewService.exportReviewToPDF(1L);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("exportReviewToPDF - not found - throws exception")
    void testExportReviewToPDF_NotFound_ThrowsException() {
        when(reviewRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () -> reviewService.exportReviewToPDF(2L));
    }

    @Test
    @DisplayName("getReviewsByEmployee - no reviews - returns empty list")
    void testGetReviewsByEmployee_NoReviews_ReturnsEmptyList() {
        when(reviewRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());
        List<PerformanceReview> result = reviewService.getReviewsByEmployee(2L);
        assertTrue(result.isEmpty());
    }
}