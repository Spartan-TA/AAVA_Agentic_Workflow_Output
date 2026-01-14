package com.example.warehouse.service;

import com.example.warehouse.entity.PerformanceReview;
import com.example.warehouse.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for PerformanceReview operations.
 */
@Service
public class PerformanceReviewService {
    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    public List<PerformanceReview> getReviewsForEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployee(employeeId);
    }

    public List<PerformanceReview> getOpenReviews() {
        return performanceReviewRepository.findAllOpenReviews();
    }

    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        review.setStatus("OPEN");
        return performanceReviewRepository.save(review);
    }

    @Transactional
    public PerformanceReview submitReview(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId).orElseThrow();
        review.setStatus("SUBMITTED");
        return performanceReviewRepository.save(review);
    }
}
