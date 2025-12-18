package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.employee.EmployeeRepository;
import com.warehouse.ems.scheduling.Shift;
import com.warehouse.ems.scheduling.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ShiftRepository shiftRepository;

    @Transactional
    public Attendance clockIn(Long employeeId, Long shiftId, String deviceInfo, String geofenceLocation) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        Attendance attendance = Attendance.builder()
                .employee(employee)
                .shift(shift)
                .clockIn(LocalDateTime.now())
                .deviceInfo(deviceInfo)
                .geofenceLocation(geofenceLocation)
                .status("PRESENT")
                .build();
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance clockOut(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        attendance.setClockOut(LocalDateTime.now());
        attendance.setStatus("COMPLETED");
        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> getAttendance(Long id) {
        return attendanceRepository.findById(id);
    }
}
