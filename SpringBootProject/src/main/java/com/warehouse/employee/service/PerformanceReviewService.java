package com.warehouse.employee.service;

import com.warehouse.employee.entity.PerformanceReview;
import com.warehouse.employee.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerformanceReviewService {
    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    public List<PerformanceReview> getAllPerformanceReviews() {
        return performanceReviewRepository.findAll();
    }

    public Optional<PerformanceReview> getPerformanceReviewById(Long id) {
        return performanceReviewRepository.findById(id);
    }

    public PerformanceReview savePerformanceReview(PerformanceReview performanceReview) {
        return performanceReviewRepository.save(performanceReview);
    }

    public void deletePerformanceReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }
}
