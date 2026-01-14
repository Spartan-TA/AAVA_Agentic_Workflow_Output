package com.example.warehouse.service;

import com.example.warehouse.entity.Attendance;
import com.example.warehouse.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Attendance operations.
 */
@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<Attendance> getAttendanceForEmployeeOnDate(Long employeeId, LocalDate date) {
        return attendanceRepository.findByEmployeeAndDate(employeeId, date);
    }

    public List<Attendance> getOpenClockIns(Long employeeId) {
        return attendanceRepository.findOpenClockInByEmployee(employeeId);
    }

    @Transactional
    public Attendance clockIn(Long employeeId, LocalDateTime clockInTime) {
        List<Attendance> openClockIns = attendanceRepository.findOpenClockInByEmployee(employeeId);
        if (!openClockIns.isEmpty()) {
            throw new IllegalStateException("Employee already clocked in and not clocked out.");
        }
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setClockInTime(clockInTime);
        attendance.setDate(clockInTime.toLocalDate());
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance clockOut(Long employeeId, LocalDateTime clockOutTime) {
        List<Attendance> openClockIns = attendanceRepository.findOpenClockInByEmployee(employeeId);
        if (openClockIns.isEmpty()) {
            throw new IllegalStateException("No open clock-in found for employee.");
        }
        Attendance attendance = openClockIns.get(0);
        attendance.setClockOutTime(clockOutTime);
        return attendanceRepository.save(attendance);
    }
}
