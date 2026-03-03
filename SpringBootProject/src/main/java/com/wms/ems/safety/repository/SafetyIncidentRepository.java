package com.wms.ems.safety.repository;

import com.wms.ems.safety.entity.SafetyIncident;
import com.wms.ems.safety.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SafetyIncident entity operations.
 * Provides CRUD operations and custom queries for safety incident management.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {

    /**
     * Finds safety incidents by status.
     * @param status the incident status
     * @return a list of safety incidents
     */
    List<SafetyIncident> findByStatus(IncidentStatus status);

    /**
     * Finds safety incidents reported between two timestamps.
     * @param start the start timestamp
     * @param end the end timestamp
     * @return a list of safety incidents
     */
    List<SafetyIncident> findByReportedAtBetween(LocalDateTime start, LocalDateTime end);
}
