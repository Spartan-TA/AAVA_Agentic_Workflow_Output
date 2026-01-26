package com.example.warehouse.review.service;

import com.example.warehouse.review.entity.PerformanceReview;
import com.example.warehouse.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    // Get all reviews
    public List<PerformanceReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Get reviews by employee
    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return reviewRepository.findByEmployeeId(employeeId);
    }

    // Get review by ID
    public Optional<PerformanceReview> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Create new review
    @Transactional
    public PerformanceReview createReview(PerformanceReview review) {
        return reviewRepository.save(review);
    }

    // Update review
    @Transactional
    public Optional<PerformanceReview> updateReview(Long id, PerformanceReview review) {
        return reviewRepository.findById(id).map(existing -> {
            existing.setReviewDate(review.getReviewDate());
            existing.setReviewer(review.getReviewer());
            existing.setRating(review.getRating());
            existing.setComments(review.getComments());
            return reviewRepository.save(existing);
        });
    }

    // Delete review
    @Transactional
    public boolean deleteReview(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
