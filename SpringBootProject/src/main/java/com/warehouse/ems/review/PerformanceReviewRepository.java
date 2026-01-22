package com.warehouse.ems.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for PerformanceReview entity.
 */
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    // Custom queries for review cycles can be added here
}
