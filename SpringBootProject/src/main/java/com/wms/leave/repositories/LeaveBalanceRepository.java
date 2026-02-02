package com.wms.leave.repositories;

import com.wms.leave.model.LeaveBalance;
import com.wms.leave.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing LeaveBalance entities
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    /**
     * Find leave balance for an employee and leave type
     * @param employeeId Employee ID
     * @param leaveType LeaveType
     * @return Optional LeaveBalance
     */
    Optional<LeaveBalance> findByEmployeeIdAndLeaveType(Long employeeId, LeaveType leaveType);
}
