package com.wms.ems.performance.repository;

import com.wms.ems.performance.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for PerformanceReview entity.
 * Provides CRUD operations and custom queries for performance reviews.
 */
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    /**
     * Find all performance reviews for a specific employee.
     * @param employeeId the employee's ID
     * @return List of PerformanceReview
     */
    List<PerformanceReview> findByEmployeeId(Long employeeId);

    /**
     * Find all performance reviews by status.
     * @param status the status of the review
     * @return List of PerformanceReview
     */
    List<PerformanceReview> findByStatus(String status);
}
