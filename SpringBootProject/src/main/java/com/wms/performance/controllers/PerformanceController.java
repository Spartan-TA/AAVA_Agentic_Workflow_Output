package com.wms.performance.controllers;

import com.wms.performance.dtos.PerformanceReviewDto;
import com.wms.performance.services.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for performance reviews
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final PerformanceService performanceService;

    /**
     * Submit a new performance review
     */
    @PostMapping("/reviews")
    public ResponseEntity<PerformanceReviewDto> submitReview(@RequestBody PerformanceReviewDto dto) {
        return ResponseEntity.ok(performanceService.submitReview(dto));
    }

    /**
     * Get all reviews for an employee
     */
    @GetMapping("/reviews/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReviewDto>> getReviewsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(performanceService.getReviewsForEmployee(employeeId));
    }
}
