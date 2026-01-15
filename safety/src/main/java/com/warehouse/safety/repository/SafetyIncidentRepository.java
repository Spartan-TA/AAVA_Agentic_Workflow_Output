package com.warehouse.safety.repository;

import com.warehouse.safety.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findBySeverity(SafetyIncident.Severity severity);
    List<SafetyIncident> findByStatus(SafetyIncident.Status status);
    List<SafetyIncident> findByLocation(String location);
}
