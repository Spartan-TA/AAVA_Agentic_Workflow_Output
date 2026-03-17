package com.warehouse.ems.service;

import com.warehouse.ems.dto.PerformanceReviewRequestDto;
import com.warehouse.ems.entity.PerformanceReview;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.PerformanceReviewRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PerformanceReviewService.
 * Covers normal operation, null/invalid input, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceTest {

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    private Employee employee;
    private PerformanceReview performanceReview;
    private PerformanceReviewRequestDto performanceReviewRequestDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        performanceReview = new PerformanceReview();
        performanceReview.setId(1L);
        performanceReview.setEmployee(employee);
        performanceReview.setReviewer("Manager");
        performanceReview.setReviewDate(LocalDate.now());
        performanceReview.setScore(4.5);
        performanceReview.setComments("Excellent");

        performanceReviewRequestDto = new PerformanceReviewRequestDto();
        performanceReviewRequestDto.setEmployeeId(1L);
        performanceReviewRequestDto.setReviewer("Manager");
        performanceReviewRequestDto.setReviewDate(LocalDate.now());
        performanceReviewRequestDto.setScore(4.5);
        performanceReviewRequestDto.setComments("Excellent");
    }

    /**
     * Test createPerformanceReview with valid input returns PerformanceReview.
     */
    @Test
    void testCreatePerformanceReview_ValidInput_ReturnsPerformanceReview() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(performanceReview);
        PerformanceReview result = performanceReviewService.createPerformanceReview(performanceReviewRequestDto);
        assertNotNull(result);
        assertEquals("Manager", result.getReviewer());
    }

    /**
     * Test createPerformanceReview with null DTO throws exception.
     */
    @Test
    void testCreatePerformanceReview_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                performanceReviewService.createPerformanceReview(null));
    }

    /**
     * Test getPerformanceReviewById with valid ID returns PerformanceReview.
     */
    @Test
    void testGetPerformanceReviewById_ValidId_ReturnsPerformanceReview() {
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(performanceReview));
        PerformanceReview result = performanceReviewService.getPerformanceReviewById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getPerformanceReviewById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetPerformanceReviewById_NonExistentId_ThrowsEntityNotFoundException() {
        when(performanceReviewRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                performanceReviewService.getPerformanceReviewById(99L));
    }

    /**
     * Test getAllPerformanceReviews returns list.
     */
    @Test
    void testGetAllPerformanceReviews_ReturnsList() {
        when(performanceReviewRepository.findAll()).thenReturn(List.of(performanceReview));
        List<PerformanceReview> result = performanceReviewService.getAllPerformanceReviews();
        assertEquals(1, result.size());
    }

    /**
     * Test updatePerformanceReview with valid input returns PerformanceReview.
     */
    @Test
    void testUpdatePerformanceReview_ValidInput_ReturnsPerformanceReview() {
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(performanceReview));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(performanceReview);
        PerformanceReview result = performanceReviewService.updatePerformanceReview(1L, performanceReviewRequestDto);
        assertNotNull(result);
    }
}
