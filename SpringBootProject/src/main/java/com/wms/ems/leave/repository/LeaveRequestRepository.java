package com.wms.ems.leave.repository;

import com.wms.ems.leave.entity.LeaveRequest;
import com.wms.ems.leave.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity operations.
 * Provides CRUD operations and custom queries for leave request management.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Finds leave requests for an employee with a specific status.
     * @param employeeId the employee ID
     * @param status the leave status
     * @return a list of leave requests
     */
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Finds leave requests by status and start date range.
     * @param status the leave status
     * @param start the start date
     * @param end the end date
     * @return a list of leave requests
     */
    List<LeaveRequest> findByStatusAndStartDateBetween(LeaveStatus status, LocalDate start, LocalDate end);
}
