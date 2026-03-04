package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.scheduling.Shift;
import com.warehouse.ems.exception.ResourceNotFoundException;
import com.warehouse.ems.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Transactional
    public Attendance clockIn(Long employeeId, Long shiftId, String deviceId, String geolocation) {
        // Business logic: Prevent duplicate clock-in for same shift/date
        List<Attendance> todayRecords = attendanceRepository.findByEmployeeAndDate(employeeId, LocalDateTime.now().toLocalDate());
        if (todayRecords.stream().anyMatch(a -> a.getClockOut() == null)) {
            throw new ValidationException("Employee already clocked in and not clocked out.");
        }
        Attendance attendance = new Attendance();
        attendance.setEmployee(new Employee(employeeId)); // Assume Employee constructor with ID
        attendance.setShift(new Shift(shiftId)); // Assume Shift constructor with ID
        attendance.setClockIn(LocalDateTime.now());
        attendance.setDeviceId(deviceId);
        attendance.setGeolocation(geolocation);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance clockOut(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found."));
        if (attendance.getClockOut() != null) {
            throw new ValidationException("Already clocked out.");
        }
        attendance.setClockOut(LocalDateTime.now());
        // Calculate hours worked
        Duration duration = Duration.between(attendance.getClockIn(), attendance.getClockOut());
        attendance.setHoursWorked(duration.toMinutes() / 60.0);
        return attendanceRepository.save(attendance);
    }

    public List<Object[]> getDailyTotals(LocalDateTime date) {
        return attendanceRepository.findDailyTotals(date.toLocalDate());
    }

    public List<Attendance> getMissedPunches() {
        return attendanceRepository.findMissedPunches();
    }

    @Transactional
    public Attendance correctAttendance(Long attendanceId, LocalDateTime newClockIn, LocalDateTime newClockOut) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found."));
        attendance.setClockIn(newClockIn);
        attendance.setClockOut(newClockOut);
        Duration duration = Duration.between(newClockIn, newClockOut);
        attendance.setHoursWorked(duration.toMinutes() / 60.0);
        return attendanceRepository.save(attendance);
    }
}
