package com.wms.performance.services;

import com.wms.performance.dtos.PerformanceReviewDto;
import com.wms.performance.model.PerformanceReview;
import com.wms.performance.repositories.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing performance reviews
 */
@Service
@RequiredArgsConstructor
public class PerformanceService {
    private final PerformanceReviewRepository performanceReviewRepository;

    /**
     * Submit a new performance review
     */
    @Transactional
    public PerformanceReviewDto submitReview(PerformanceReviewDto dto) {
        PerformanceReview review = PerformanceReview.builder()
                .employeeId(dto.getEmployeeId())
                .reviewerId(dto.getReviewerId())
                .periodStart(dto.getPeriodStart())
                .periodEnd(dto.getPeriodEnd())
                .rating(dto.getRating())
                .comments(dto.getComments())
                .goals(dto.getGoals())
                .build();
        PerformanceReview saved = performanceReviewRepository.save(review);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all reviews for an employee
     */
    public List<PerformanceReviewDto> getReviewsForEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId).stream()
                .map(r -> PerformanceReviewDto.builder()
                        .id(r.getId())
                        .employeeId(r.getEmployeeId())
                        .reviewerId(r.getReviewerId())
                        .periodStart(r.getPeriodStart())
                        .periodEnd(r.getPeriodEnd())
                        .rating(r.getRating())
                        .comments(r.getComments())
                        .goals(r.getGoals())
                        .build())
                .collect(Collectors.toList());
    }
}
