package com.warehouse.performance.service;

import com.warehouse.performance.entity.PerformanceReview;
import com.warehouse.performance.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PerformanceReviewService {
    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    public List<PerformanceReview> getAllReviews() {
        return performanceReviewRepository.findAll();
    }

    public Optional<PerformanceReview> getReviewById(Long id) {
        return performanceReviewRepository.findById(id);
    }

    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    public List<PerformanceReview> getReviewsByRating(PerformanceReview.Rating rating) {
        return performanceReviewRepository.findByRating(rating);
    }

    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        return performanceReviewRepository.save(review);
    }

    @Transactional
    public PerformanceReview updateReview(Long id, PerformanceReview updated) {
        PerformanceReview review = performanceReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setReviewDate(updated.getReviewDate());
        review.setReviewer(updated.getReviewer());
        review.setComments(updated.getComments());
        review.setRating(updated.getRating());
        return performanceReviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }

    public byte[] exportReviewToPDF(Long id) {
        PerformanceReview review = performanceReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        // Simulate PDF export logic
        String pdfContent = "Performance Review
" +
                "Employee ID: " + review.getEmployeeId() + "
" +
                "Reviewer: " + review.getReviewer() + "
" +
                "Date: " + review.getReviewDate() + "
" +
                "Rating: " + review.getRating() + "
" +
                "Comments: " + review.getComments();
        return pdfContent.getBytes();
    }
}
