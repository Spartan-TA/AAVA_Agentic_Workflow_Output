package com.wms.ems.repository;

import com.wms.ems.entity.SafetyIncident;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.SafetyIncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for SafetyIncident entity operations.
 * Provides CRUD and custom query methods for safety incident management.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    /**
     * Find safety incidents by status.
     * @param status the incident status
     * @return List of SafetyIncidents
     */
    List<SafetyIncident> findByStatus(SafetyIncidentStatus status);

    /**
     * Find safety incidents within a date range.
     * @param startDate start date
     * @param endDate end date
     * @return List of SafetyIncidents
     */
    List<SafetyIncident> findByIncidentDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find safety incidents reported by a specific employee.
     * @param reportedBy the reporting employee
     * @return List of SafetyIncidents
     */
    List<SafetyIncident> findByReportedBy(Employee reportedBy);
}
