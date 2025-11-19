package com.example.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public PerformanceReview saveReview(PerformanceReview review) {
        return performanceReviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }
}