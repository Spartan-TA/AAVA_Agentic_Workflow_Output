package com.wms.ems.leave.repository;

import com.wms.ems.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity.
 * Provides CRUD operations and custom queries for leave requests.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    /**
     * Find all leave requests for a specific employee.
     * @param employeeId the employee's ID
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    /**
     * Find all leave requests by status.
     * @param status the status of the leave request
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByStatus(String status);
}
