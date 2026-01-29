package com.company.wms.attendance.service;

import com.company.wms.attendance.model.AttendanceRecord;
import com.company.wms.attendance.repository.AttendanceRepository;
import com.company.wms.employee.model.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for attendance business logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public AttendanceRecord getAttendanceRecordById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
    }

    public List<AttendanceRecord> getAttendanceByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return attendanceRepository.findByEmployee(employee);
    }

    public List<AttendanceRecord> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public AttendanceRecord createAttendanceRecord(AttendanceRecord record) {
        return attendanceRepository.save(record);
    }

    @Transactional
    public AttendanceRecord updateAttendanceRecord(Long id, AttendanceRecord updatedRecord) {
        AttendanceRecord record = getAttendanceRecordById(id);
        record.setStatus(updatedRecord.getStatus());
        record.setCheckInTime(updatedRecord.getCheckInTime());
        record.setCheckOutTime(updatedRecord.getCheckOutTime());
        return attendanceRepository.save(record);
    }

    public void deleteAttendanceRecord(Long id) {
        attendanceRepository.deleteById(id);
    }
}
