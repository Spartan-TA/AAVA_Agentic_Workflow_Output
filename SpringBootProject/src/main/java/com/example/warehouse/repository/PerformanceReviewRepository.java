package com.example.warehouse.repository;

import com.example.warehouse.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository for PerformanceReview entity.
 */
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    @Query("SELECT p FROM PerformanceReview p WHERE p.employee.id = :employeeId")
    List<PerformanceReview> findByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT p FROM PerformanceReview p WHERE p.status = 'OPEN'")
    List<PerformanceReview> findAllOpenReviews();
}
