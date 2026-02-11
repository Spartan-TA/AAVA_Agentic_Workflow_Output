package com.warehouse.employee.repository;

import com.warehouse.employee.domain.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SafetyIncident entity.
 */
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
}
