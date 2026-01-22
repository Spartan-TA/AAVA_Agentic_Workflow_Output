package com.warehouse.ems.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for PerformanceReview endpoints.
 */
@RestController
@RequestMapping("/reviews")
@Validated
public class PerformanceReviewController {
    private final PerformanceReviewService reviewService;

    @Autowired
    public PerformanceReviewController(PerformanceReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a new performance review.
     */
    @PostMapping
    public ResponseEntity<PerformanceReview> createReview(@Valid @RequestBody PerformanceReview review) {
        try {
            PerformanceReview created = reviewService.createReview(review);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all reviews.
     */
    @GetMapping
    public ResponseEntity<List<PerformanceReview>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    /**
     * Acknowledge a review.
     */
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<PerformanceReview> acknowledgeReview(@PathVariable Long id) {
        try {
            PerformanceReview acknowledged = reviewService.acknowledgeReview(id);
            return ResponseEntity.ok(acknowledged);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Export review as PDF.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportReviewPdf(@PathVariable Long id) {
        try {
            byte[] pdf = reviewService.exportReviewPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "review-" + id + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
