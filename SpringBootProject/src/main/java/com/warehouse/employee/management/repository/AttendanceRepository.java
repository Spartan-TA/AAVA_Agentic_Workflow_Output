package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Attendance entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    @Query("SELECT a FROM Attendance a WHERE a.deletedAt IS NULL")
    List<Attendance> findAllActive();

    @Query("SELECT a FROM Attendance a WHERE a.deletedAt IS NULL")
    Page<Attendance> findAllActive(Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Attendance> findActiveById(Long id);

    // Custom query example: Find by employeeId and date
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.date = :date AND a.deletedAt IS NULL")
    Optional<Attendance> findActiveByEmployeeIdAndDate(Long employeeId, java.time.LocalDate date);
}
