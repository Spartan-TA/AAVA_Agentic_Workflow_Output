package com.company.wms.scheduling.repository;

import com.company.wms.scheduling.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ShiftTemplate entity.
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    Optional<ShiftTemplate> findByName(String name);
}
