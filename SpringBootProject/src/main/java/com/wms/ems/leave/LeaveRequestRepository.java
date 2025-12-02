package com.wms.ems.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    @Query("SELECT SUM(l.accrualBalance) FROM LeaveRequest l WHERE l.employeeId = :employeeId AND l.type = :type AND l.status = 'Approved'")
    double findBalanceByEmployeeIdAndType(Long employeeId, String type);
}
