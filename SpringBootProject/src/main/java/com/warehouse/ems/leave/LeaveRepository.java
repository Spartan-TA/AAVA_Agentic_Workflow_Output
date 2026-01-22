package com.warehouse.ems.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing LeaveRequest entities.
 */
@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests for a given employee.
     * @param employeeId Employee ID
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    /**
     * Find all leave requests for a given status.
     * @param status Leave status
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByStatus(String status);

    /**
     * Find all leave requests that overlap with a given date range.
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.employeeId = :employeeId AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaves(Long employeeId, LocalDate startDate, LocalDate endDate);
}
