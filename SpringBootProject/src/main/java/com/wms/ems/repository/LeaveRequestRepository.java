package com.wms.ems.repository;

import com.wms.ems.entity.LeaveRequest;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity operations.
 * Provides CRUD and custom query methods for leave request management.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    /**
     * Find leave requests by employee and status.
     * @param employee the employee
     * @param status the leave request status
     * @return List of LeaveRequests
     */
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveRequestStatus status);

    /**
     * Find leave requests by approver and status.
     * @param approver the approver
     * @param status the leave request status
     * @return List of LeaveRequests
     */
    List<LeaveRequest> findByApproverAndStatus(Employee approver, LeaveRequestStatus status);

    /**
     * Find leave requests within a date range.
     * @param startDate start date
     * @param endDate end date
     * @return List of LeaveRequests
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
