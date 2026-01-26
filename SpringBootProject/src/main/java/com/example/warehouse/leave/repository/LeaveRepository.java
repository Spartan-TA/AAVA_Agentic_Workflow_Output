package com.example.warehouse.leave.repository;

import com.example.warehouse.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    // Find all leave requests for an employee
    List<LeaveRequest> findByEmployeeId(Long employeeId);
}
