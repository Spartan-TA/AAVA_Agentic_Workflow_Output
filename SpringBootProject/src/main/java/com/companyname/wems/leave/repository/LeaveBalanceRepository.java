package com.companyname.wems.leave.repository;

import com.companyname.wems.leave.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    LeaveBalance findByEmployeeIdAndLeaveType(Long employeeId, String leaveType);
}