package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.domain.PerformanceReview;
import com.warehouse.employee.domain.PerformanceReview.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for PerformanceReview entity.
 */
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    /**
     * Find reviews by employee.
     */
    List<PerformanceReview> findByEmployee(Employee employee);

    /**
     * Find reviews by review period.
     */
    List<PerformanceReview> findByReviewPeriod(String reviewPeriod);

    /**
     * Find reviews by status.
     */
    List<PerformanceReview> findByStatus(Status status);
}
