package com.wms.ems.repository;

import com.wms.ems.entity.LeaveBalance;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LeaveBalance entity operations.
 * Provides CRUD and custom query methods for leave balance management.
 */
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    /**
     * Find leave balances by employee.
     * @param employee the employee
     * @return List of LeaveBalances
     */
    List<LeaveBalance> findByEmployee(Employee employee);

    /**
     * Find leave balance by employee and leave type.
     * @param employee the employee
     * @param leaveType the leave type
     * @return Optional of LeaveBalance
     */
    Optional<LeaveBalance> findByEmployeeAndLeaveType(Employee employee, LeaveType leaveType);
}
