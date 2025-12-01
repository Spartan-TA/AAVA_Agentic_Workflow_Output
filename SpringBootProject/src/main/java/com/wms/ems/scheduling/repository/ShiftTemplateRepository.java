package com.wms.ems.scheduling.repository;

import com.wms.ems.scheduling.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for ShiftTemplate entity.
 * Provides CRUD operations and custom queries for shift templates.
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    /**
     * Find all shift templates by active status.
     * @param active whether the template is active
     * @return List of ShiftTemplate
     */
    List<ShiftTemplate> findByActiveTrue();
}
