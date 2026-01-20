package com.wms.scheduling.repository;

import com.wms.scheduling.domain.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ShiftTemplate entity.
 * Provides CRUD operations for shift templates.
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    // Custom query methods can be defined here if needed
}
