package com.wms.scheduling.repositories;

import com.wms.scheduling.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing ShiftTemplate entities
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    /**
     * Find a shift template by name
     * @param name Name of the shift
     * @return Optional ShiftTemplate
     */
    Optional<ShiftTemplate> findByName(String name);
}
