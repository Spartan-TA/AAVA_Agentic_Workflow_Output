package com.company.project.controller;

import com.company.project.dto.PerformanceReviewDto;
import com.company.project.service.PerformanceReviewService;
import com.company.project.mapper.PerformanceReviewMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/performance-reviews")
@Tag(name = "Performance Review Management", description = "Manage employee performance reviews")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;
    private final PerformanceReviewMapper performanceReviewMapper;

    @Autowired
    public PerformanceReviewController(PerformanceReviewService performanceReviewService, PerformanceReviewMapper performanceReviewMapper) {
        this.performanceReviewService = performanceReviewService;
        this.performanceReviewMapper = performanceReviewMapper;
    }

    @Operation(summary = "Create performance review", responses = {
            @ApiResponse(responseCode = "201", description = "Performance review created successfully")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> createReview(@Valid @RequestBody PerformanceReviewDto request) {
        var review = performanceReviewService.createReview(request);
        return ResponseEntity.status(201).body(performanceReviewMapper.toDto(review));
    }

    @Operation(summary = "Get reviews for employee", responses = {
            @ApiResponse(responseCode = "200", description = "List of performance reviews")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReviewDto>> getReviewsByEmployee(@PathVariable Long employeeId) {
        var reviews = performanceReviewService.getReviewsByEmployee(employeeId);
        return ResponseEntity.ok(performanceReviewMapper.toDtoList(reviews));
    }
}
