package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for LeaveBalance entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long>, JpaSpecificationExecutor<LeaveBalance> {
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.deletedAt IS NULL")
    List<LeaveBalance> findAllActive();

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.deletedAt IS NULL")
    Page<LeaveBalance> findAllActive(Pageable pageable);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.id = :id AND lb.deletedAt IS NULL")
    Optional<LeaveBalance> findActiveById(Long id);

    // Custom query example: Find by employeeId and leaveType
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.leaveType = :leaveType AND lb.deletedAt IS NULL")
    Optional<LeaveBalance> findActiveByEmployeeIdAndLeaveType(Long employeeId, String leaveType);
}
