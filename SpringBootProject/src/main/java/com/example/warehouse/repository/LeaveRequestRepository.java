package com.example.warehouse.repository;

import com.example.warehouse.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for LeaveRequest entity.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.id = :employeeId AND l.startDate >= :from AND l.endDate <= :to")
    List<LeaveRequest> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING_APPROVAL'")
    List<LeaveRequest> findAllPendingApproval();
}
