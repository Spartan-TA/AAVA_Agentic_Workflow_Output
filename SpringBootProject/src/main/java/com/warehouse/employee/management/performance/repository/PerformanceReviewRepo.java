package com.warehouse.employee.management.performance.repository;

import com.warehouse.employee.management.performance.domain.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceReviewRepo extends JpaRepository<PerformanceReview, Long> {
    // Custom query methods if needed
}
