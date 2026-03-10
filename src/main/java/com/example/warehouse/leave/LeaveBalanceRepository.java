package com.example.warehouse.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    LeaveBalance findByEmployeeId(Long employeeId);
}
