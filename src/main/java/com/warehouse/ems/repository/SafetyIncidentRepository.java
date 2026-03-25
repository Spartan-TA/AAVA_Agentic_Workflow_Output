package com.warehouse.ems.repository;

import com.warehouse.ems.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
}
