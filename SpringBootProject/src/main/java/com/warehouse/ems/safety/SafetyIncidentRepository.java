package com.warehouse.ems.safety;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SafetyIncident entity.
 */
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    // Custom queries for OSHA reporting can be added here
}
