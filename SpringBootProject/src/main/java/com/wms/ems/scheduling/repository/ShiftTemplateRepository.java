package com.wms.ems.scheduling.repository;

import com.wms.ems.scheduling.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for ShiftTemplate entity operations.
 * Provides CRUD operations and custom queries for shift template management.
 */
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {

    /**
     * Finds all recurring shift templates.
     * @return a list of recurring shift templates
     */
    List<ShiftTemplate> findByRecurringTrue();
}
