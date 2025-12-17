package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for PerformanceReview entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long>, JpaSpecificationExecutor<PerformanceReview> {
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.deletedAt IS NULL")
    List<PerformanceReview> findAllActive();

    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.deletedAt IS NULL")
    Page<PerformanceReview> findAllActive(Pageable pageable);

    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.id = :id AND pr.deletedAt IS NULL")
    Optional<PerformanceReview> findActiveById(Long id);

    // Custom query example: Find by employeeId and reviewPeriod
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.id = :employeeId AND pr.reviewPeriod = :reviewPeriod AND pr.deletedAt IS NULL")
    List<PerformanceReview> findActiveByEmployeeIdAndReviewPeriod(Long employeeId, String reviewPeriod);
}
