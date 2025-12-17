package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for LeaveRequest entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.deletedAt IS NULL")
    List<LeaveRequest> findAllActive();

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.deletedAt IS NULL")
    Page<LeaveRequest> findAllActive(Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.id = :id AND lr.deletedAt IS NULL")
    Optional<LeaveRequest> findActiveById(Long id);

    // Custom query example: Find by employeeId and status
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = :status AND lr.deletedAt IS NULL")
    List<LeaveRequest> findActiveByEmployeeIdAndStatus(Long employeeId, String status);
}
