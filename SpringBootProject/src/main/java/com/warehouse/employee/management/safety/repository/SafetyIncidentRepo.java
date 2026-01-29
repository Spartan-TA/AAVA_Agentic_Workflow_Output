package com.warehouse.employee.management.safety.repository;

import com.warehouse.employee.management.safety.domain.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyIncidentRepo extends JpaRepository<SafetyIncident, Long> {
    // Custom query methods if needed
}
