package com.example.ems.repository;

import com.example.ems.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    // Custom query methods if needed
}
