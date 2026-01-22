package com.warehouse.ems.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing performance reviews, workflow, and PDF export.
 */
@Service
public class PerformanceReviewService {
    private final PerformanceReviewRepository reviewRepository;

    @Autowired
    public PerformanceReviewService(PerformanceReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Create a new performance review (review cycle).
     */
    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        review.setStatus(PerformanceReview.Status.DRAFT);
        return reviewRepository.save(review);
    }

    /**
     * Get all reviews.
     */
    public List<PerformanceReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * Submit a review (change status to SUBMITTED).
     */
    @Transactional
    public PerformanceReview submitReview(Long id) {
        PerformanceReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
        review.setStatus(PerformanceReview.Status.SUBMITTED);
        return reviewRepository.save(review);
    }

    /**
     * Acknowledge a review (change status to ACKNOWLEDGED and set date).
     */
    @Transactional
    public PerformanceReview acknowledgeReview(Long id) {
        PerformanceReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
        review.setStatus(PerformanceReview.Status.ACKNOWLEDGED);
        review.setAcknowledgedDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    /**
     * Export review as PDF (stub - implement PDF logic as needed).
     */
    public byte[] exportReviewPdf(Long id) {
        // Stub: In production, use a PDF library to generate the PDF
        PerformanceReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
        return ("PDF for review " + review.getId()).getBytes();
    }
}
