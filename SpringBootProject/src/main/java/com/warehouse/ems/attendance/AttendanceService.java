package com.warehouse.ems.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Attendance business logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public AttendanceEvent clockIn(Long employeeId, Long shiftId) {
        AttendanceEvent event = AttendanceEvent.builder()
                .employeeId(employeeId)
                .shiftId(shiftId)
                .clockIn(LocalDateTime.now())
                .build();
        return attendanceRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(Long eventId) {
        AttendanceEvent event = attendanceRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance event not found"));
        event.setClockOut(LocalDateTime.now());
        if (event.getClockIn() != null && event.getClockOut() != null) {
            event.setHoursWorked(Duration.between(event.getClockIn(), event.getClockOut()).toMinutes() / 60.0);
        }
        return attendanceRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceReport(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    // Missed punch correction logic can be added here
}
