package com.example.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/performance-reviews")
public class PerformanceReviewController {
    @Autowired
    private PerformanceReviewService performanceReviewService;

    @GetMapping
    public List<PerformanceReview> getAllReviews() {
        return performanceReviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public Optional<PerformanceReview> getReviewById(@PathVariable Long id) {
        return performanceReviewService.getReviewById(id);
    }

    @PostMapping
    public PerformanceReview createReview(@RequestBody PerformanceReview review) {
        return performanceReviewService.saveReview(review);
    }

    @PutMapping("/{id}")
    public PerformanceReview updateReview(@PathVariable Long id, @RequestBody PerformanceReview review) {
        review.setId(id);
        return performanceReviewService.saveReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        performanceReviewService.deleteReview(id);
    }
}