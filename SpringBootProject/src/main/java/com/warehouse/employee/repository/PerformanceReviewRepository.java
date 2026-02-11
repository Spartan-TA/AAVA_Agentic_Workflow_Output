package com.warehouse.employee.repository;

import com.warehouse.employee.domain.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for PerformanceReview entity.
 */
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
}
