package com.warehouse.employee.repository;

import com.warehouse.employee.domain.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ShiftTemplate entity.
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
}
