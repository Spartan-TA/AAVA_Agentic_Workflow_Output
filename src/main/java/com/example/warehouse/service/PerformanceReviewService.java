package com.example.warehouse.service;

import com.example.warehouse.dto.PerformanceReviewDTO;
import com.example.warehouse.entity.PerformanceReview;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.PerformanceReviewRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository, EmployeeRepository employeeRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public PerformanceReview createReview(Long employeeId, PerformanceReviewDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setCycle(dto.getCycle());
        review.setGoals(dto.getGoals());
        review.setStatus("DRAFT");
        review.setCreatedDate(LocalDate.now());
        return performanceReviewRepository.save(review);
    }

    @Transactional
    public PerformanceReview acknowledgeReview(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (review.getStatus().equals("SIGNED_OFF")) {
            throw new IllegalArgumentException("Review is immutable after sign-off");
        }
        review.setStatus("SIGNED_OFF");
        performanceReviewRepository.save(review);
        // PDF export logic
        return review;
    }

    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    // PDF export logic would be handled by a separate utility/service
}
