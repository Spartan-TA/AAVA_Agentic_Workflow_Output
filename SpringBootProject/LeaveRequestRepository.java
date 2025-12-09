package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByStartDateBetween(LocalDate start, LocalDate end);
}
