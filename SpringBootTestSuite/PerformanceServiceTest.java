package com.example.warehouse.test;

import com.example.warehouse.performance.PerformanceReview;
import com.example.warehouse.performance.PerformanceRepository;
import com.example.warehouse.performance.PerformanceService;
import com.example.warehouse.performance.PerformanceController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformanceServiceTest {
    @Mock
    private PerformanceRepository performanceRepository;
    @InjectMocks
    private PerformanceService performanceService;
    private PerformanceController performanceController;
    private PerformanceReview testReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        performanceController = new PerformanceController(performanceService);
        testReview = new PerformanceReview(1L, 1L, "Q1 2024", LocalDate.now(), "PENDING", "", "");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testCreateReview_ValidInput_Success() {
        when(performanceRepository.save(any(PerformanceReview.class))).thenReturn(testReview);
        PerformanceReview created = performanceService.createReview(testReview);
        assertNotNull(created);
        assertEquals("Q1 2024", created.getCycle());
    }

    @Test
    void testCreateReview_Duplicate_ThrowsException() {
        when(performanceRepository.findByEmployeeIdAndCycle(1L, "Q1 2024")).thenReturn(Optional.of(testReview));
        assertThrows(IllegalArgumentException.class, () -> performanceService.createReview(testReview));
    }

    @Test
    void testGetReviewById_ValidId_ReturnsReview() {
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(testReview));
        PerformanceReview found = performanceService.getReviewById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetReviewById_InvalidId_ThrowsException() {
        when(performanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> performanceService.getReviewById(2L));
    }

    @Test
    void testAcknowledgeReview_Valid_Success() {
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(performanceRepository.save(any(PerformanceReview.class))).thenReturn(testReview);
        PerformanceReview ack = performanceService.acknowledgeReview(1L);
        assertEquals("ACKNOWLEDGED", ack.getStatus());
    }

    @Test
    void testAcknowledgeReview_InvalidId_ThrowsException() {
        when(performanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> performanceService.acknowledgeReview(2L));
    }

    @Test
    void testController_CreateReview_Success() {
        when(performanceService.createReview(any(PerformanceReview.class))).thenReturn(testReview);
        ResponseEntity<PerformanceReview> response = performanceController.createReview(testReview);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Q1 2024", response.getBody().getCycle());
    }

    @Test
    void testController_CreateReview_Duplicate() {
        when(performanceService.createReview(any(PerformanceReview.class))).thenThrow(new IllegalArgumentException("Duplicate"));
        assertThrows(IllegalArgumentException.class, () -> performanceController.createReview(testReview));
    }
}
