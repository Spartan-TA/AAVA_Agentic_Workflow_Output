import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import javax.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
public class PerformanceReviewServiceTest {
    @Mock
    private PerformanceReviewRepository performanceReviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private PerformanceReviewServiceImpl performanceReviewService;

    private PerformanceReviewDto validReviewDto;
    private PerformanceReview validReview;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");

        validReviewDto = new PerformanceReviewDto();
        validReviewDto.setEmployeeId(1L);
        validReviewDto.setReviewer("Manager");
        validReviewDto.setScore(4);
        validReviewDto.setComments("Good performance");

        validReview = new PerformanceReview();
        validReview.setId(1L);
        validReview.setEmployee(validEmployee);
        validReview.setReviewer("Manager");
        validReview.setScore(4);
        validReview.setComments("Good performance");
    }

    @Test
    void testCreateReview_ValidInput() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(validReview);
        PerformanceReview result = performanceReviewService.create(validReviewDto);
        assertNotNull(result);
        assertEquals("Manager", result.getReviewer());
        verify(performanceReviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    void testCreateReview_NullReviewer() {
        validReviewDto.setReviewer(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> performanceReviewService.create(validReviewDto));
    }

    @Test
    void testCreateReview_InvalidScore() {
        validReviewDto.setScore(6);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        assertThrows(ValidationException.class, () -> performanceReviewService.create(validReviewDto));
    }

    @Test
    void testCreateReview_NonExistentEmployee() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        validReviewDto.setEmployeeId(2L);
        assertThrows(ResourceNotFoundException.class, () -> performanceReviewService.create(validReviewDto));
    }

    @Test
    void testUpdateReview_ValidInput() {
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(validReview));
        PerformanceReviewDto updateDto = new PerformanceReviewDto();
        updateDto.setReviewer("Supervisor");
        updateDto.setScore(5);
        updateDto.setComments("Excellent");
        validReview.setReviewer("Supervisor");
        validReview.setScore(5);
        validReview.setComments("Excellent");
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(validReview);
        PerformanceReview result = performanceReviewService.update(1L, updateDto);
        assertEquals("Supervisor", result.getReviewer());
        assertEquals(5, result.getScore());
        assertEquals("Excellent", result.getComments());
    }

    @Test
    void testUpdateReview_NonExistentId() {
        when(performanceReviewRepository.findById(2L)).thenReturn(Optional.empty());
        PerformanceReviewDto updateDto = new PerformanceReviewDto();
        updateDto.setReviewer("Supervisor");
        updateDto.setScore(5);
        updateDto.setComments("Excellent");
        assertThrows(ResourceNotFoundException.class, () -> performanceReviewService.update(2L, updateDto));
    }

    @Test
    void testDeleteReview_ValidId() {
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(validReview));
        doNothing().when(performanceReviewRepository).deleteById(1L);
        assertDoesNotThrow(() -> performanceReviewService.delete(1L));
        verify(performanceReviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteReview_NonExistentId() {
        when(performanceReviewRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> performanceReviewService.delete(2L));
    }

    @Test
    void testGetReview_ValidId() {
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(validReview));
        PerformanceReview result = performanceReviewService.get(1L);
        assertNotNull(result);
        assertEquals("Manager", result.getReviewer());
    }

    @Test
    void testGetReview_NonExistentId() {
        when(performanceReviewRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> performanceReviewService.get(2L));
    }

    @Test
    void testListReviews_WithResults() {
        List<PerformanceReview> reviews = Arrays.asList(validReview);
        when(performanceReviewRepository.findAll()).thenReturn(reviews);
        List<PerformanceReview> result = performanceReviewService.list();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListReviews_EmptyResult() {
        when(performanceReviewRepository.findAll()).thenReturn(Collections.emptyList());
        List<PerformanceReview> result = performanceReviewService.list();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}