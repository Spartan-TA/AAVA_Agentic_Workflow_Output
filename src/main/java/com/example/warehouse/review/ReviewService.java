package com.example.warehouse.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<PerformanceReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<PerformanceReview> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return reviewRepository.findByEmployeeId(employeeId);
    }

    public PerformanceReview createReview(ReviewDto dto) {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(dto.getEmployeeId());
        review.setReviewDate(dto.getReviewDate());
        review.setReviewer(dto.getReviewer());
        review.setComments(dto.getComments());
        review.setRating(dto.getRating());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
