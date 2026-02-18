package com.companyname.wem.leave.repository;

import com.companyname.wem.leave.domain.LeaveRequest;
import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.leave.domain.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByStartDateBetween(LocalDate start, LocalDate end);
}
