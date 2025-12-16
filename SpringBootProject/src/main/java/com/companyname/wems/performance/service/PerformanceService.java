package com.companyname.wems.performance.service;

import com.companyname.wems.performance.model.PerformanceReview;
import com.companyname.wems.performance.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceService {
    private final PerformanceReviewRepository performanceReviewRepository;

    // Create review
    public PerformanceReview createReview(PerformanceReview review) {
        return performanceReviewRepository.save(review);
    }

    // Get reviews for employee
    public List<PerformanceReview> getReviewsForEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    // Get reviews by reviewer
    public List<PerformanceReview> getReviewsByReviewer(Long reviewerId) {
        return performanceReviewRepository.findByReviewerId(reviewerId);
    }

    // Update review
    public PerformanceReview updateReview(Long id, PerformanceReview updated) {
        PerformanceReview review = performanceReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PerformanceReview not found"));
        review.setReviewCycle(updated.getReviewCycle());
        review.setReviewDate(updated.getReviewDate());
        review.setRating(updated.getRating());
        review.setGoals(updated.getGoals());
        review.setCompetencies(updated.getCompetencies());
        review.setComments(updated.getComments());
        review.setReviewerId(updated.getReviewerId());
        return performanceReviewRepository.save(review);
    }

    // Delete review
    public void deleteReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }
}