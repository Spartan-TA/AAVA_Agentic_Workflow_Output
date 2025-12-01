package com.wms.ems.safety.repository;

import com.wms.ems.safety.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for SafetyIncident entity.
 * Provides CRUD operations and custom queries for safety incidents.
 */
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    /**
     * Find all safety incidents reported by a specific employee.
     * @param reporterId the reporter's employee ID
     * @return List of SafetyIncident
     */
    List<SafetyIncident> findByReporterId(Long reporterId);

    /**
     * Find all safety incidents by status.
     * @param status the status of the incident
     * @return List of SafetyIncident
     */
    List<SafetyIncident> findByStatus(String status);
}
