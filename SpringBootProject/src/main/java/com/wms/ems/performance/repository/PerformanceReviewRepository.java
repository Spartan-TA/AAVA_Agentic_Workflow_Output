package com.wms.ems.performance.repository;

import com.wms.ems.performance.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for PerformanceReview entity operations.
 * Provides CRUD operations and custom queries for performance review management.
 */
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    /**
     * Finds performance reviews by employee ID.
     * @param employeeId the employee ID
     * @return a list of performance reviews
     */
    List<PerformanceReview> findByEmployeeId(Long employeeId);

    /**
     * Finds performance reviews by cycle.
     * @param cycle the review cycle
     * @return a list of performance reviews
     */
    List<PerformanceReview> findByCycle(String cycle);
}
