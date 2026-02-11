package com.warehouse.employee.repository;

import com.warehouse.employee.domain.LeaveRequest;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LeaveRequest entity with custom query methods.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find leave requests by employee and status.
     * @param employee Employee
     * @param status Status
     * @return List of LeaveRequest
     */
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, String status);
}
