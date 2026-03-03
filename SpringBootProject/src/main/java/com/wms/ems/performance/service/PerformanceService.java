package com.wms.ems.performance.service;

import com.wms.ems.performance.repository.PerformanceReviewRepository;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.performance.entity.PerformanceReview;
import com.wms.ems.performance.dto.PerformanceReviewDto;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing performance reviews.
 */
@Service
@Transactional
@Slf4j
public class PerformanceService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new performance review.
     * @param dto PerformanceReviewDto
     * @return PerformanceReview
     */
    public PerformanceReview createReview(PerformanceReviewDto dto) {
        if (dto == null || dto.getEmployeeId() == null) {
            log.error("Validation failed: Employee ID is required");
            throw new ValidationException("Employee ID is required");
        }
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setCycle(dto.getCycle());
        review.setScore(dto.getScore());
        review.setComments(dto.getComments());
        review.setAcknowledgedByEmployee(false);
        review.setAcknowledgedBySupervisor(false);
        try {
            PerformanceReview saved = performanceReviewRepository.save(review);
            log.info("Performance review created: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create performance review", e);
            throw new BusinessException("Failed to create performance review");
        }
    }

    /**
     * Updates a performance review if not acknowledged.
     * @param reviewId Review ID
     * @param dto PerformanceReviewDto
     * @return PerformanceReview
     */
    public PerformanceReview updateReview(Long reviewId, PerformanceReviewDto dto) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found"));
        if (review.isAcknowledgedByEmployee() || review.isAcknowledgedBySupervisor()) {
            log.error("Cannot update acknowledged review");
            throw new ValidationException("Cannot update acknowledged review");
        }
        review.setScore(dto.getScore());
        review.setComments(dto.getComments());
        try {
            PerformanceReview updated = performanceReviewRepository.save(review);
            log.info("Performance review {} updated", reviewId);
            return updated;
        } catch (Exception e) {
            log.error("Failed to update performance review", e);
            throw new BusinessException("Failed to update performance review");
        }
    }

    /**
     * Supervisor acknowledges a review.
     * @param reviewId Review ID
     * @param supervisor Supervisor name
     * @return PerformanceReview
     */
    public PerformanceReview acknowledgeBySupervisor(Long reviewId, String supervisor) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found"));
        review.setAcknowledgedBySupervisor(true);
        review.setSupervisor(supervisor);
        try {
            PerformanceReview updated = performanceReviewRepository.save(review);
            log.info("Performance review {} acknowledged by supervisor {}", reviewId, supervisor);
            return updated;
        } catch (Exception e) {
            log.error("Failed to acknowledge review by supervisor", e);
            throw new BusinessException("Failed to acknowledge review by supervisor");
        }
    }

    /**
     * Employee acknowledges a review.
     * @param reviewId Review ID
     * @return PerformanceReview
     */
    public PerformanceReview acknowledgeByEmployee(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found"));
        review.setAcknowledgedByEmployee(true);
        try {
            PerformanceReview updated = performanceReviewRepository.save(review);
            log.info("Performance review {} acknowledged by employee", reviewId);
            return updated;
        } catch (Exception e) {
            log.error("Failed to acknowledge review by employee", e);
            throw new BusinessException("Failed to acknowledge review by employee");
        }
    }

    /**
     * Gets all reviews for an employee.
     * @param employeeId Employee ID
     * @return List of PerformanceReview
     */
    @Transactional(readOnly = true)
    public List<PerformanceReview> getEmployeeReviews(Long employeeId) {
        try {
            return performanceReviewRepository.findByEmployeeId(employeeId);
        } catch (Exception e) {
            log.error("Failed to fetch employee reviews", e);
            throw new BusinessException("Failed to fetch employee reviews");
        }
    }

    /**
     * Gets reviews by cycle.
     * @param cycle Review cycle
     * @return List of PerformanceReview
     */
    @Transactional(readOnly = true)
    public List<PerformanceReview> getReviewsByCycle(String cycle) {
        try {
            return performanceReviewRepository.findByCycle(cycle);
        } catch (Exception e) {
            log.error("Failed to fetch reviews by cycle", e);
            throw new BusinessException("Failed to fetch reviews by cycle");
        }
    }
}
