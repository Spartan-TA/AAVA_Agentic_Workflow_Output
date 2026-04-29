package com.example.ems.service;

import com.example.ems.entity.PerformanceReview;
import com.example.ems.repository.PerformanceReviewRepository;
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

    public PerformanceReview createReview(PerformanceReview review) {
        return performanceReviewRepository.save(review);
    }

    public PerformanceReview updateReview(Long id, PerformanceReview updatedReview) {
        return performanceReviewRepository.findById(id)
                .map(existing -> {
                    existing.setCycle(updatedReview.getCycle());
                    existing.setReviewDate(updatedReview.getReviewDate());
                    existing.setReviewer(updatedReview.getReviewer());
                    existing.setStatus(updatedReview.getStatus());
                    existing.setComments(updatedReview.getComments());
                    existing.setOverallRating(updatedReview.getOverallRating());
                    return performanceReviewRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("PerformanceReview not found"));
    }

    public void deleteReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }
}
