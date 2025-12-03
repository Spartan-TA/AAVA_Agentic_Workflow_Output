package com.company.wems.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);

    @Query("SELECT a FROM AttendanceEvent a WHERE a.employee.id = :employeeId AND a.timestamp BETWEEN :start AND :end")
    List<AttendanceEvent> findEventsForEmployeeBetween(@Param("employeeId") Long employeeId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    List<AttendanceEvent> findByApprovalStatus(AttendanceEvent.ApprovalStatus status);
}
