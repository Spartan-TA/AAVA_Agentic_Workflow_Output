package com.example.warehouse.repository;

import com.example.warehouse.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(String status);
    List<SafetyIncident> findBySeverity(String severity);
}
