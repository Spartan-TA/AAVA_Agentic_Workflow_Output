package com.wms.leave.repositories;

import com.wms.leave.model.LeaveRequest;
import com.wms.leave.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing LeaveRequest entities
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    /**
     * Find all leave requests for an employee
     * @param employeeId Employee ID
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    /**
     * Find all leave requests by status
     * @param status LeaveStatus
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByStatus(LeaveStatus status);
}
