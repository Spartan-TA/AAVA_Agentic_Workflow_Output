package com.example.warehouse.review.controller;

import com.example.warehouse.review.entity.PerformanceReview;
import com.example.warehouse.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    // Get all reviews
    @GetMapping
    public List<PerformanceReview> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // Get reviews by employee
    @GetMapping("/employee/{employeeId}")
    public List<PerformanceReview> getReviewsByEmployee(@PathVariable Long employeeId) {
        return reviewService.getReviewsByEmployee(employeeId);
    }

    // Get review by ID
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReview> getReviewById(@PathVariable Long id) {
        Optional<PerformanceReview> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new review
    @PostMapping
    public ResponseEntity<PerformanceReview> createReview(@RequestBody PerformanceReview review) {
        PerformanceReview created = reviewService.createReview(review);
        return ResponseEntity.ok(created);
    }

    // Update review
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceReview> updateReview(@PathVariable Long id, @RequestBody PerformanceReview review) {
        Optional<PerformanceReview> updated = reviewService.updateReview(id, review);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
