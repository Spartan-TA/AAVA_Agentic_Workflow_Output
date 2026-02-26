package com.warehouse.employee.repository;

import com.warehouse.employee.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
}
