package com.wms.leave.repository;

import com.wms.leave.domain.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity.
 * Provides CRUD operations and custom queries for leave management.
 */
@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
}
