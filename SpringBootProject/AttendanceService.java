package com.example.warehousemanagement.service;

import com.example.warehousemanagement.entity.AttendanceEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Attendance business logic.
 */
public interface AttendanceService {
    AttendanceEvent getAttendanceEventById(Long id);
    List<AttendanceEvent> getAttendanceEventsByEmployee(Long employeeId);
    List<AttendanceEvent> getAttendanceEventsByDate(LocalDate date);
    AttendanceEvent createAttendanceEvent(AttendanceEvent event);
    AttendanceEvent updateAttendanceEvent(Long id, AttendanceEvent event);
    void deleteAttendanceEvent(Long id);
}
