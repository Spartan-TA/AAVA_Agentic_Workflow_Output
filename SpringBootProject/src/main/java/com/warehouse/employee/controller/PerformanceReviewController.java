package com.warehouse.employee.controller;

import com.warehouse.employee.dto.PerformanceReviewDto;
import com.warehouse.employee.service.PerformanceReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for performance reviews.
 */
@RestController
@RequestMapping("/api/performance-reviews")
@Validated
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;

    @Autowired
    public PerformanceReviewController(PerformanceReviewService performanceReviewService) {
        this.performanceReviewService = performanceReviewService;
    }

    @Operation(summary = "Create a performance review for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> createReview(@Valid @RequestBody PerformanceReviewDto dto) {
        PerformanceReviewDto response = performanceReviewService.createReview(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Acknowledge a performance review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review acknowledged successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','MANAGER')")
    @PutMapping("/acknowledge/{reviewId}")
    public ResponseEntity<PerformanceReviewDto> acknowledgeReview(@PathVariable Long reviewId) {
        PerformanceReviewDto response = performanceReviewService.acknowledgeReview(reviewId);
        return ResponseEntity.ok(response);
    }
}
