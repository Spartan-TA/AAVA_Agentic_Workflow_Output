package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}