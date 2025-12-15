package com.warehouseems.attendance.service;

import com.warehouseems.attendance.dto.ClockInDto;
import com.warehouseems.attendance.dto.ClockOutDto;
import com.warehouseems.attendance.entity.AttendanceEvent;
import com.warehouseems.attendance.entity.AttendanceEventType;
import com.warehouseems.attendance.repository.AttendanceEventRepository;
import com.warehouseems.employee.service.EmployeeService;
import com.warehouseems.scheduling.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeService employeeService;
    private final ShiftService shiftService;

    @Transactional
    public AttendanceEvent clockIn(ClockInDto dto) {
        var employee = employeeService.findByBadgeId(dto.getBadgeId());
        LocalDate today = LocalDate.now();
        List<AttendanceEvent> todayEvents = attendanceEventRepository.findTodayEvents(employee.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        boolean alreadyClockedIn = todayEvents.stream().anyMatch(e -> e.getType() == AttendanceEventType.CLOCK_IN);
        if (alreadyClockedIn) throw new IllegalStateException("Already clocked in today");
        if (!shiftService.isEmployeeScheduledNow(employee.getId())) throw new IllegalStateException("Not scheduled for shift now");
        AttendanceEvent event = AttendanceEvent.builder()
                .employeeId(employee.getId())
                .type(AttendanceEventType.CLOCK_IN)
                .deviceId(dto.getDeviceId())
                .location(dto.getLocation())
                .ipAddress(dto.getIpAddress())
                .approved(false)
                .notes(null)
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(ClockOutDto dto) {
        var employee = employeeService.findByBadgeId(dto.getBadgeId());
        List<AttendanceEvent> lastEvents = attendanceEventRepository.findLastEvent(employee.getId());
        if (lastEvents.isEmpty() || lastEvents.get(0).getType() != AttendanceEventType.CLOCK_IN) {
            throw new IllegalStateException("No active clock-in found");
        }
        AttendanceEvent event = AttendanceEvent.builder()
                .employeeId(employee.getId())
                .type(AttendanceEventType.CLOCK_OUT)
                .deviceId(dto.getDeviceId())
                .location(dto.getLocation())
                .ipAddress(dto.getIpAddress())
                .approved(false)
                .notes(null)
                .build();
        return attendanceEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AttendanceEvent> getEmployeeAttendance(Long employeeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return attendanceEventRepository.findByEmployeeIdAndTimestampBetween(employeeId, start, end);
    }
}
