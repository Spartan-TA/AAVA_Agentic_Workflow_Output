package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Shift entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift> {
    @Query("SELECT s FROM Shift s WHERE s.deletedAt IS NULL")
    List<Shift> findAllActive();

    @Query("SELECT s FROM Shift s WHERE s.deletedAt IS NULL")
    Page<Shift> findAllActive(Pageable pageable);

    @Query("SELECT s FROM Shift s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Shift> findActiveById(Long id);

    // Custom query example: Find by shiftType
    @Query("SELECT s FROM Shift s WHERE s.shiftType = :shiftType AND s.deletedAt IS NULL")
    List<Shift> findActiveByShiftType(String shiftType);
}
