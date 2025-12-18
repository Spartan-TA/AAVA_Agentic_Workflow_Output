package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.dto.ClockInRequestDTO;
import com.warehouse.ems.attendance.dto.ClockOutRequestDTO;
import com.warehouse.ems.attendance.dto.AttendanceResponseDTO;
import com.warehouse.ems.attendance.entity.AttendanceEvent;
import com.warehouse.ems.attendance.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AttendanceService with hours calculation logic.
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public AttendanceResponseDTO clockIn(ClockInRequestDTO request) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_IN);
        event.setEventTime(LocalDateTime.now());
        attendanceRepository.save(event);
        return toResponseDTO(event);
    }

    @Override
    public AttendanceResponseDTO clockOut(ClockOutRequestDTO request) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_OUT);
        event.setEventTime(LocalDateTime.now());
        attendanceRepository.save(event);
        return toResponseDTO(event);
    }

    @Override
    public List<AttendanceResponseDTO> getAttendanceForEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateWorkedHours(Long employeeId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.atTime(LocalTime.MAX);
        List<AttendanceEvent> events = attendanceRepository.findEventsForEmployeeInPeriod(employeeId, start, end);
        double totalHours = 0.0;
        LocalDateTime lastClockIn = null;
        for (AttendanceEvent event : events) {
            if (event.getEventType() == AttendanceEvent.EventType.CLOCK_IN) {
                lastClockIn = event.getEventTime();
            } else if (event.getEventType() == AttendanceEvent.EventType.CLOCK_OUT && lastClockIn != null) {
                totalHours += java.time.Duration.between(lastClockIn, event.getEventTime()).toMinutes() / 60.0;
                lastClockIn = null;
            }
        }
        return totalHours;
    }

    private AttendanceResponseDTO toResponseDTO(AttendanceEvent event) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setId(event.getId());
        dto.setEmployeeId(event.getEmployeeId());
        dto.setEventType(event.getEventType());
        dto.setEventTime(event.getEventTime());
        return dto;
    }
}
