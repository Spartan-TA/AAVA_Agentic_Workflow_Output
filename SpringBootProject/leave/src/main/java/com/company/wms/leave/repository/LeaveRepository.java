package com.company.wms.leave.repository;

import com.company.wms.leave.domain.LeaveRequest;
import com.company.wms.leave.domain.LeaveRequest.LeaveStatus;
import com.company.wms.leave.domain.LeaveRequest.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity.
 * Provides CRUD operations and custom queries for leave management.
 */
@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests for a specific employee ordered by creation date.
     * @param employeeId the employee ID
     * @param pageable pagination information
     * @return page of leave requests
     */
    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    /**
     * Find all leave requests with a specific status.
     * @param status the leave status
     * @param pageable pagination information
     * @return page of leave requests
     */
    Page<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status, Pageable pageable);

    /**
     * Find all leave requests for an employee with a specific type and status.
     * @param employeeId the employee ID
     * @param leaveType the leave type
     * @param status the leave status
     * @return list of leave requests
     */
    List<LeaveRequest> findByEmployeeIdAndLeaveTypeAndStatus(
        Long employeeId, 
        LeaveType leaveType, 
        LeaveStatus status
    );

    /**
     * Find overlapping leave requests for an employee within a date range.
     * @param employeeId the employee ID
     * @param startDate the start date
     * @param endDate the end date
     * @param status the leave status to check
     * @return list of overlapping leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.status = :status " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") LeaveStatus status
    );

    /**
     * Find all leave requests within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of leave requests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(lr.startDate <= :endDate AND lr.endDate >= :startDate)")
    List<LeaveRequest> findLeaveRequestsInDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count pending leave requests for an employee.
     * @param employeeId the employee ID
     * @return count of pending requests
     */
    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Find all leave requests approved by a specific approver.
     * @param approverId the approver ID
     * @param pageable pagination information
     * @return page of leave requests
     */
    Page<LeaveRequest> findByApproverIdOrderByApprovedAtDesc(Long approverId, Pageable pageable);
}