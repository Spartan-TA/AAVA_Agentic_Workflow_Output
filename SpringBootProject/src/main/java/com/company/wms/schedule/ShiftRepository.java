package com.company.wms.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing ShiftTemplate entities.
 */
@Repository
public interface ShiftRepository extends JpaRepository<ShiftTemplate, Long> {
    // Additional query methods can be defined here
}
