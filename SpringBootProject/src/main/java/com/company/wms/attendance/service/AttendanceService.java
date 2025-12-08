package com.company.wms.attendance.service;

import com.company.wms.attendance.entity.AttendanceEvent;
import com.company.wms.attendance.repository.AttendanceEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance business logic.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final AttendanceEventRepository attendanceEventRepository;

    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location, Long shiftId) {
        AttendanceEvent event = AttendanceEvent.builder()
                .employeeId(employeeId)
                .type("CLOCK_IN")
                .timestamp(LocalDateTime.now())
                .deviceId(deviceId)
                .location(location)
                .shiftId(shiftId)
                .build();
        log.info("Clock-in event: {}", event);
        return attendanceEventRepository.save(event);
    }

    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location, Long shiftId, Double hoursWorked) {
        AttendanceEvent event = AttendanceEvent.builder()
                .employeeId(employeeId)
                .type("CLOCK_OUT")
                .timestamp(LocalDateTime.now())
                .deviceId(deviceId)
                .location(location)
                .shiftId(shiftId)
                .hoursWorked(hoursWorked)
                .build();
        log.info("Clock-out event: {}", event);
        return attendanceEventRepository.save(event);
    }

    public List<AttendanceEvent> getAttendanceEventsForEmployee(Long employeeId) {
        return attendanceEventRepository.findByEmployeeId(employeeId);
    }
}
