package com.example.warehouse.repository;

import com.example.warehouse.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for SafetyIncident entity.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    @Query("SELECT s FROM SafetyIncident s WHERE s.date BETWEEN :from AND :to")
    List<SafetyIncident> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT s FROM SafetyIncident s WHERE s.status = 'OPEN'")
    List<SafetyIncident> findAllOpenIncidents();
}
