package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.service.PerformanceReviewService;
import com.example.repository.EmployeeRepository;
import com.example.repository.PerformanceReviewRepository;
import com.example.model.Employee;
import com.example.model.PerformanceReview;
import com.example.exception.EmployeeNotFoundException;
import com.example.exception.PerformanceReviewAlreadySubmittedException;
import com.example.exception.PerformanceReviewNotSubmittedException;

@ExtendWith(MockitoExtension.class)
public class PerformanceReviewServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PerformanceReviewRepository reviewRepository;
    @InjectMocks
    private PerformanceReviewService reviewService;

    private Employee employee;
    private PerformanceReview review;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        review = new PerformanceReview();
        review.setId(1L);
        review.setEmployee(employee);
        review.setStatus("DRAFT");
    }

    @Test
    void testCreateReview_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        PerformanceReview result = reviewService.createReview(1L, "Q1 Review");
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        verify(reviewRepository).save(any(PerformanceReview.class));
    }

    @Test
    void testCreateReview_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> reviewService.createReview(2L, "Q1 Review"));
    }

    @Test
    void testSubmitReview_Success() {
        review.setStatus("DRAFT");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        PerformanceReview result = reviewService.submitReview(1L);
        assertEquals("SUBMITTED", result.getStatus());
        verify(reviewRepository).save(review);
    }

    @Test
    void testSubmitReview_AlreadySubmitted_ThrowsException() {
        review.setStatus("SUBMITTED");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertThrows(PerformanceReviewAlreadySubmittedException.class, () -> reviewService.submitReview(1L));
    }

    @Test
    void testAcknowledgeReview_Success() {
        review.setStatus("SUBMITTED");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        PerformanceReview result = reviewService.acknowledgeReview(1L);
        assertEquals("ACKNOWLEDGED", result.getStatus());
        verify(reviewRepository).save(review);
    }

    @Test
    void testAcknowledgeReview_NotSubmitted_ThrowsException() {
        review.setStatus("DRAFT");
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertThrows(PerformanceReviewNotSubmittedException.class, () -> reviewService.acknowledgeReview(1L));
    }

    @Test
    void testGetReviewsByEmployee_ReturnsCorrectList() {
        List<PerformanceReview> reviews = Arrays.asList(review);
        when(reviewRepository.findByEmployeeId(1L)).thenReturn(reviews);
        List<PerformanceReview> result = reviewService.getReviewsByEmployee(1L);
        assertEquals(1, result.size());
        assertEquals(employee, result.get(0).getEmployee());
    }

    @Test
    void testExportReviewToPDF_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        byte[] pdf = reviewService.exportReviewToPDF(1L);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
