package com.warehouse.employee.management.performance.service;

import com.warehouse.employee.management.dto.PerformanceReviewDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PerformanceService {
    private final List<PerformanceReviewDto> reviews = new ArrayList<>();

    @Transactional
    public PerformanceReviewDto addReview(PerformanceReviewDto review) {
        reviews.add(review);
        return review;
    }

    public List<PerformanceReviewDto> getReviewsByEmployee(Long employeeId) {
        List<PerformanceReviewDto> result = new ArrayList<>();
        for (PerformanceReviewDto r : reviews) {
            if (r.getEmployeeId().equals(employeeId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<PerformanceReviewDto> getAllReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public void exportReviewToPdf(Long employeeId) {
        // Stub for PDF export logic
    }
}
