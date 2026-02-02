package com.wms.performance.repositories;

import com.wms.performance.model.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing PerformanceReview entities
 */
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    /**
     * Find all reviews for an employee
     * @param employeeId Employee ID
     * @return List of PerformanceReview
     */
    List<PerformanceReview> findByEmployeeId(Long employeeId);
}
