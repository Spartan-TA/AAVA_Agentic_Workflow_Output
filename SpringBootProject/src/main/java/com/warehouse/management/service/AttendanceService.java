package com.warehouse.management.service;

import com.warehouse.management.entity.Attendance;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    Attendance createAttendance(Attendance attendance);
    Attendance updateAttendance(Long id, Attendance attendance);
    void deleteAttendance(Long id);
    Optional<Attendance> getAttendanceById(Long id);
    List<Attendance> getAllAttendances();
}
