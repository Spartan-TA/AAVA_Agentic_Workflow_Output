package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for AuditLog entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    @Query("SELECT al FROM AuditLog al WHERE al.deletedAt IS NULL")
    List<AuditLog> findAllActive();

    @Query("SELECT al FROM AuditLog al WHERE al.deletedAt IS NULL")
    Page<AuditLog> findAllActive(Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.id = :id AND al.deletedAt IS NULL")
    Optional<AuditLog> findActiveById(Long id);

    // Custom query example: Find by actionType
    @Query("SELECT al FROM AuditLog al WHERE al.actionType = :actionType AND al.deletedAt IS NULL")
    List<AuditLog> findActiveByActionType(String actionType);
}
