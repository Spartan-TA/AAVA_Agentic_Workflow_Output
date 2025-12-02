package com.wms.ems.safety;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
}
