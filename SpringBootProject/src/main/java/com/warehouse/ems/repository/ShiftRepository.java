package com.warehouse.ems.repository;

import com.warehouse.ems.domain.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ShiftTemplate entity.
 */
@Repository
public interface ShiftRepository extends JpaRepository<ShiftTemplate, Long> {
}
