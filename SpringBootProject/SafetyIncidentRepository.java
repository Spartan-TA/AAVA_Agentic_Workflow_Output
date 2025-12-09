package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for SafetyIncident entity.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findBySeverity(SafetyIncident.Severity severity);
    List<SafetyIncident> findByWorkflowStatus(SafetyIncident.WorkflowStatus status);
    List<SafetyIncident> findByLocation(String location);
}
