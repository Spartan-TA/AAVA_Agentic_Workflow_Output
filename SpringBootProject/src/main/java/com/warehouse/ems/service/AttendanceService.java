package com.warehouse.ems.service;

import com.warehouse.ems.domain.AttendanceRecord;
import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Attendance business logic.
 */
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Clock in for an employee.
     */
    @Transactional
    public AttendanceRecord clockIn(Employee employee, LocalDateTime clockInTime) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setClockIn(clockInTime);
        return attendanceRepository.save(record);
    }

    /**
     * Clock out for an employee.
     */
    @Transactional
    public AttendanceRecord clockOut(Long recordId, LocalDateTime clockOutTime) {
        Optional<AttendanceRecord> recordOpt = attendanceRepository.findById(recordId);
        if (recordOpt.isPresent()) {
            AttendanceRecord record = recordOpt.get();
            record.setClockOut(clockOutTime);
            return attendanceRepository.save(record);
        }
        throw new RuntimeException("Attendance record not found");
    }

    /**
     * Request correction for an attendance record.
     */
    @Transactional
    public void requestCorrection(Long recordId) {
        attendanceRepository.findById(recordId).ifPresent(record -> {
            record.setCorrectionRequested(true);
            attendanceRepository.save(record);
        });
    }

    /**
     * Export attendance records for an employee within a date range.
     */
    public List<AttendanceRecord> exportAttendance(Employee employee, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByEmployeeAndClockInBetween(employee, start, end);
    }
}
