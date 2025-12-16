package com.companyname.wems.performance.controller;

import com.companyname.wems.performance.model.PerformanceReview;
import com.companyname.wems.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final PerformanceService performanceService;

    // Create review
    @PostMapping("/review")
    public ResponseEntity<PerformanceReview> createReview(@RequestBody PerformanceReview review) {
        return ResponseEntity.ok(performanceService.createReview(review));
    }

    // Get reviews for employee
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<PerformanceReview>> getReviewsForEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getReviewsForEmployee(id));
    }

    // Get reviews by reviewer
    @GetMapping("/reviewer/{id}")
    public ResponseEntity<List<PerformanceReview>> getReviewsByReviewer(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getReviewsByReviewer(id));
    }

    // Update review
    @PutMapping("/review/{id}")
    public ResponseEntity<PerformanceReview> updateReview(@PathVariable Long id, @RequestBody PerformanceReview updated) {
        return ResponseEntity.ok(performanceService.updateReview(id, updated));
    }

    // Delete review
    @DeleteMapping("/review/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        performanceService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}