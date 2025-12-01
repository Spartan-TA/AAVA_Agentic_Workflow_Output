package com.wms.ems.performance.service;

import com.wms.ems.performance.entity.PerformanceReview;
import com.wms.ems.performance.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for Performance Review management.
 * Handles review workflow and PDF export.
 */
@Service
@Transactional
public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
    }

    /**
     * Submit a new performance review.
     * @param review the performance review
     * @return the saved PerformanceReview
     */
    public PerformanceReview submitReview(PerformanceReview review) {
        review.setStatus("SUBMITTED");
        // ... set other fields as needed
        return performanceReviewRepository.save(review);
    }

    /**
     * Approve a performance review.
     * @param reviewId the review ID
     * @return the updated PerformanceReview
     */
    public PerformanceReview approveReview(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Performance review not found"));
        review.setStatus("APPROVED");
        return performanceReviewRepository.save(review);
    }

    /**
     * Get all reviews for an employee.
     * @param employeeId the employee's ID
     * @return List of PerformanceReview
     */
    public List<PerformanceReview> getReviewsForEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    /**
     * Export a performance review to PDF (stub).
     * @param reviewId the review ID
     * @return byte[] representing the PDF file
     */
    public byte[] exportReviewToPdf(Long reviewId) {
        // Implement PDF export logic here (stub)
        return new byte[0];
    }
}
