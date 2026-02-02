package com.wms.safety.repositories;

import com.wms.safety.model.SafetyIncident;
import com.wms.safety.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing SafetyIncident entities
 */
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    /**
     * Find all incidents by status
     * @param status IncidentStatus
     * @return List of SafetyIncident
     */
    List<SafetyIncident> findByStatus(IncidentStatus status);
}
