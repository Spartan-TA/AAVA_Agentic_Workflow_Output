package com.company.wems.attendance;

import com.company.wems.employee.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;

    /**
     * Clock in for an employee.
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Transactional
    public AttendanceEvent clockIn(Employee employee, String device, String location) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceEvent.AttendanceType.CLOCK_IN);
        event.setDevice(device);
        event.setLocation(location);
        event.setApprovalStatus(AttendanceEvent.ApprovalStatus.PENDING);
        return attendanceEventRepository.save(event);
    }

    /**
     * Clock out for an employee and calculate total hours.
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Transactional
    public AttendanceEvent clockOut(Employee employee) {
        List<AttendanceEvent> events = attendanceEventRepository.findByEmployeeId(employee.getId());
        Optional<AttendanceEvent> lastClockIn = events.stream()
                .filter(e -> e.getType() == AttendanceEvent.AttendanceType.CLOCK_IN && e.getClockOutTime() == null)
                .reduce((first, second) -> second); // get last clock-in
        if (lastClockIn.isEmpty()) {
            throw new IllegalStateException("No active clock-in found for employee.");
        }
        AttendanceEvent clockInEvent = lastClockIn.get();
        AttendanceEvent clockOutEvent = new AttendanceEvent();
        clockOutEvent.setEmployee(employee);
        clockOutEvent.setTimestamp(LocalDateTime.now());
        clockOutEvent.setType(AttendanceEvent.AttendanceType.CLOCK_OUT);
        clockOutEvent.setDevice(clockInEvent.getDevice());
        clockOutEvent.setLocation(clockInEvent.getLocation());
        clockOutEvent.setApprovalStatus(AttendanceEvent.ApprovalStatus.PENDING);
        clockOutEvent.setClockOutTime(LocalDateTime.now());
        double hours = Duration.between(clockInEvent.getTimestamp(), clockOutEvent.getClockOutTime()).toMinutes() / 60.0;
        clockOutEvent.setTotalHours(hours);
        attendanceEventRepository.save(clockOutEvent);
        // update clock-in event with clockOutTime and totalHours
        clockInEvent.setClockOutTime(clockOutEvent.getClockOutTime());
        clockInEvent.setTotalHours(hours);
        attendanceEventRepository.save(clockInEvent);
        return clockOutEvent;
    }

    /**
     * Get attendance history for an employee.
     */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public List<AttendanceEvent> getAttendanceHistory(Long employeeId) {
        return attendanceEventRepository.findByEmployeeId(employeeId);
    }
}
