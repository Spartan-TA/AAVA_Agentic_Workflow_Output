package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.PerformanceReview;
import com.warehouse.employee.dto.PerformanceReviewDto;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.PerformanceReviewMapper;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for performance review management.
 */
@Service
public class PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;
    private final PerformanceReviewMapper performanceReviewMapper;

    @Autowired
    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository,
                                    EmployeeRepository employeeRepository,
                                    PerformanceReviewMapper performanceReviewMapper) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
        this.performanceReviewMapper = performanceReviewMapper;
    }

    /**
     * Create a new performance review for an employee.
     * @param dto PerformanceReviewDto
     * @return PerformanceReviewDto
     */
    @Transactional
    public PerformanceReviewDto createReview(PerformanceReviewDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + dto.getEmployeeId()));
        PerformanceReview review = performanceReviewMapper.toEntity(dto);
        review.setEmployee(employee);
        review.setAcknowledged(false);
        PerformanceReview saved = performanceReviewRepository.save(review);
        return performanceReviewMapper.toDto(saved);
    }

    /**
     * Acknowledge a performance review.
     * @param reviewId Review ID
     * @return PerformanceReviewDto
     */
    @Transactional
    public PerformanceReviewDto acknowledgeReview(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Performance review not found: " + reviewId));
        review.setAcknowledged(true);
        PerformanceReview updated = performanceReviewRepository.save(review);
        return performanceReviewMapper.toDto(updated);
    }
}
