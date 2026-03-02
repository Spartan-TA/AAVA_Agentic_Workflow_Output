package com.wems.safety.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
}
