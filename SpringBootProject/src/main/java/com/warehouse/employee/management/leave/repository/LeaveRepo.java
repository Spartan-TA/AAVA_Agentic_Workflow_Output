package com.warehouse.employee.management.leave.repository;

import com.warehouse.employee.management.leave.domain.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Long> {
    // Custom query methods if needed
}
