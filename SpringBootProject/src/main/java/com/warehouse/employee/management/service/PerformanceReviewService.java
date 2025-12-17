package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.PerformanceReview;
import com.warehouse.employee.management.repository.PerformanceReviewRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing PerformanceReview entities.
 */
@Service
public class PerformanceReviewService {
    private final PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    public PerformanceReviewService(PerformanceReviewRepository performanceReviewRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
    }

    /**
     * Get all performance reviews.
     * @return List of performance reviews
     */
    public List<PerformanceReview> getAllPerformanceReviews() {
        return performanceReviewRepository.findAll();
    }

    /**
     * Get performance review by ID.
     * @param id PerformanceReview ID
     * @return PerformanceReview entity
     */
    public PerformanceReview getPerformanceReviewById(Long id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview not found with id: " + id));
    }

    /**
     * Create a new performance review.
     * @param performanceReview PerformanceReview entity
     * @return Created performance review
     */
    @Transactional
    public PerformanceReview createPerformanceReview(PerformanceReview performanceReview) {
        return performanceReviewRepository.save(performanceReview);
    }

    /**
     * Update an existing performance review.
     * @param id PerformanceReview ID
     * @param updatedReview Updated performance review entity
     * @return Updated performance review
     */
    @Transactional
    public PerformanceReview updatePerformanceReview(Long id, PerformanceReview updatedReview) {
        PerformanceReview existingReview = getPerformanceReviewById(id);
        existingReview.setEmployee(updatedReview.getEmployee());
        existingReview.setReviewDate(updatedReview.getReviewDate());
        existingReview.setReviewer(updatedReview.getReviewer());
        existingReview.setScore(updatedReview.getScore());
        existingReview.setComments(updatedReview.getComments());
        // Add other fields as needed
        return performanceReviewRepository.save(existingReview);
    }

    /**
     * Delete a performance review by ID.
     * @param id PerformanceReview ID
     */
    @Transactional
    public void deletePerformanceReview(Long id) {
        PerformanceReview review = getPerformanceReviewById(id);
        performanceReviewRepository.delete(review);
    }

    /**
     * Start a new review cycle for an employee.
     * @param employeeId Employee ID
     * @return Created performance review
     */
    @Transactional
    public PerformanceReview startReviewCycle(Long employeeId) {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(employeeId);
        review.setStatus("IN_PROGRESS");
        // Set other default fields as needed
        return performanceReviewRepository.save(review);
    }
}
