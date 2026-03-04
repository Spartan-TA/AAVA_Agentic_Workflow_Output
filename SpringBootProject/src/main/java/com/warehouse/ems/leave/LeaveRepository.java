package com.warehouse.ems.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId")
    List<LeaveRequest> findByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.type = :type")
    LeaveBalance findBalanceByEmployeeAndType(@Param("employeeId") Long employeeId, @Param("type") String type);
}
