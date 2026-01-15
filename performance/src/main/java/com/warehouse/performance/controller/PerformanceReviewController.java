package com.warehouse.performance.controller;

import com.warehouse.performance.entity.PerformanceReview;
import com.warehouse.performance.service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/performance/reviews")
public class PerformanceReviewController {
    @Autowired
    private PerformanceReviewService performanceReviewService;

    @GetMapping
    public ResponseEntity<List<PerformanceReview>> getAllReviews() {
        return ResponseEntity.ok(performanceReviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReview> getReviewById(@PathVariable Long id) {
        return performanceReviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReview>> getReviewsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(performanceReviewService.getReviewsByEmployee(employeeId));
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<PerformanceReview>> getReviewsByRating(@PathVariable PerformanceReview.Rating rating) {
        return ResponseEntity.ok(performanceReviewService.getReviewsByRating(rating));
    }

    @PostMapping
    public ResponseEntity<PerformanceReview> createReview(@Valid @RequestBody PerformanceReview review) {
        PerformanceReview created = performanceReviewService.createReview(review);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceReview> updateReview(@PathVariable Long id, @Valid @RequestBody PerformanceReview review) {
        PerformanceReview updated = performanceReviewService.updateReview(id, review);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        performanceReviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportReviewToPDF(@PathVariable Long id) {
        byte[] pdf = performanceReviewService.exportReviewToPDF(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "review-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
