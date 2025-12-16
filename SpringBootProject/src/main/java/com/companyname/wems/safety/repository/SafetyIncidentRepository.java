package com.companyname.wems.safety.repository;

import com.companyname.wems.safety.model.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(String status);
    List<SafetyIncident> findByIncidentDateBetween(LocalDate start, LocalDate end);
    List<SafetyIncident> findBySeverity(String severity);
}