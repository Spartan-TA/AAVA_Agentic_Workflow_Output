package com.example.ems.controller;

import com.example.ems.dto.PerformanceReviewDto;
import com.example.ems.service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/performance-reviews")
@Validated
public class PerformanceReviewController {
    private final PerformanceReviewService performanceReviewService;

    @Autowired
    public PerformanceReviewController(PerformanceReviewService performanceReviewService) {
        this.performanceReviewService = performanceReviewService;
    }

    @GetMapping
    public ResponseEntity<List<PerformanceReviewDto>> getAllPerformanceReviews() {
        return ResponseEntity.ok(performanceReviewService.getAllPerformanceReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> getPerformanceReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(performanceReviewService.getPerformanceReviewById(id));
    }

    @PostMapping
    public ResponseEntity<PerformanceReviewDto> createPerformanceReview(@Valid @RequestBody PerformanceReviewDto performanceReviewDto) {
        return ResponseEntity.ok(performanceReviewService.createPerformanceReview(performanceReviewDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> updatePerformanceReview(@PathVariable Long id, @Valid @RequestBody PerformanceReviewDto performanceReviewDto) {
        return ResponseEntity.ok(performanceReviewService.updatePerformanceReview(id, performanceReviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformanceReview(@PathVariable Long id) {
        performanceReviewService.deletePerformanceReview(id);
        return ResponseEntity.noContent().build();
    }
}