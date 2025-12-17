package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for SafetyIncident entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long>, JpaSpecificationExecutor<SafetyIncident> {
    @Query("SELECT si FROM SafetyIncident si WHERE si.deletedAt IS NULL")
    List<SafetyIncident> findAllActive();

    @Query("SELECT si FROM SafetyIncident si WHERE si.deletedAt IS NULL")
    Page<SafetyIncident> findAllActive(Pageable pageable);

    @Query("SELECT si FROM SafetyIncident si WHERE si.id = :id AND si.deletedAt IS NULL")
    Optional<SafetyIncident> findActiveById(Long id);

    // Custom query example: Find by severity
    @Query("SELECT si FROM SafetyIncident si WHERE si.severity = :severity AND si.deletedAt IS NULL")
    List<SafetyIncident> findActiveBySeverity(String severity);
}
