package com.wms.ems.repository;

import com.wms.ems.entity.PerformanceReview;
import com.wms.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for PerformanceReview entity operations.
 * Provides CRUD and custom query methods for performance review management.
 */
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    /**
     * Find performance reviews by employee and review cycle.
     * @param employee the employee
     * @param reviewCycle the review cycle
     * @return List of PerformanceReviews
     */
    List<PerformanceReview> findByEmployeeAndReviewCycle(Employee employee, String reviewCycle);

    /**
     * Find performance reviews by supervisor.
     * @param supervisor the supervisor
     * @return List of PerformanceReviews
     */
    List<PerformanceReview> findBySupervisor(Employee supervisor);
}
