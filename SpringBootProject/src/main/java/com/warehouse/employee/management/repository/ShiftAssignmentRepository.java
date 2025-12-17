package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for ShiftAssignment entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long>, JpaSpecificationExecutor<ShiftAssignment> {
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.deletedAt IS NULL")
    List<ShiftAssignment> findAllActive();

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.deletedAt IS NULL")
    Page<ShiftAssignment> findAllActive(Pageable pageable);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.id = :id AND sa.deletedAt IS NULL")
    Optional<ShiftAssignment> findActiveById(Long id);

    // Custom query example: Find by employeeId and shiftId
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id = :employeeId AND sa.shift.id = :shiftId AND sa.deletedAt IS NULL")
    List<ShiftAssignment> findActiveByEmployeeIdAndShiftId(Long employeeId, Long shiftId);
}
