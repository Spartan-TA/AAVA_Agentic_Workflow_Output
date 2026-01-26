package com.example.warehouse.review.repository;

import com.example.warehouse.review.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<PerformanceReview, Long> {
    // Find reviews by employee
    List<PerformanceReview> findByEmployeeId(Long employeeId);
}
