package com.wms.ems.repository;

import com.wms.ems.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShiftTemplate entity operations.
 * Provides CRUD and custom query methods for shift template management.
 */
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    /**
     * Find all active shift templates.
     * @return List of active ShiftTemplates
     */
    List<ShiftTemplate> findByIsActiveTrue();

    /**
     * Find a shift template by name.
     * @param name the shift template name
     * @return Optional of ShiftTemplate
     */
    Optional<ShiftTemplate> findByName(String name);
}
