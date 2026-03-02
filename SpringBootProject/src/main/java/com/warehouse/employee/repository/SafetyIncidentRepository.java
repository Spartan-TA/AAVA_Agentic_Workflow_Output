package com.warehouse.employee.repository;

import com.warehouse.employee.domain.SafetyIncident;
import com.warehouse.employee.domain.SafetyIncident.Status;
import com.warehouse.employee.domain.SafetyIncident.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SafetyIncident entity.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    /**
     * Find incidents by status.
     */
    List<SafetyIncident> findByStatus(Status status);

    /**
     * Find incidents by severity.
     */
    List<SafetyIncident> findBySeverity(Severity severity);

    /**
     * Find incidents between two incident dates.
     */
    List<SafetyIncident> findByIncidentDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find incidents by location.
     */
    List<SafetyIncident> findByLocation(String location);
}
