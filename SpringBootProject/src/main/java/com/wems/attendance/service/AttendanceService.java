package com.wems.attendance.service;

import com.wems.attendance.domain.*;
import com.wems.employee.domain.Employee;
import com.wems.scheduling.domain.Schedule;
import com.wems.attendance.dto.ClockEventDto;
import com.wems.common.exception.ResourceNotFoundException;
import com.wems.common.exception.BusinessValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceEventRepository attendanceEventRepository;

    public AttendanceEvent clockIn(Employee employee, ClockEventDto dto, Schedule schedule) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setType(EventType.CLOCK_IN);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(dto.getDeviceId());
        event.setLocation(dto.getLocation());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setStatus(EventStatus.NORMAL);
        event.setSchedule(schedule);
        return attendanceEventRepository.save(event);
    }

    public AttendanceEvent clockOut(Employee employee, ClockEventDto dto, Schedule schedule) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setType(EventType.CLOCK_OUT);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(dto.getDeviceId());
        event.setLocation(dto.getLocation());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setStatus(EventStatus.NORMAL);
        event.setSchedule(schedule);
        return attendanceEventRepository.save(event);
    }

    public long calculateHoursWorked(Employee employee, LocalDateTime from, LocalDateTime to) {
        List<AttendanceEvent> events = attendanceEventRepository.findAll(); // Replace with proper query
        LocalDateTime lastIn = null;
        long totalMinutes = 0;
        for (AttendanceEvent event : events) {
            if (event.getEmployee().equals(employee) &&
                !event.getTimestamp().isBefore(from) &&
                !event.getTimestamp().isAfter(to)) {
                if (event.getType() == EventType.CLOCK_IN) {
                    lastIn = event.getTimestamp();
                } else if (event.getType() == EventType.CLOCK_OUT && lastIn != null) {
                    totalMinutes += Duration.between(lastIn, event.getTimestamp()).toMinutes();
                    lastIn = null;
                }
            }
        }
        return totalMinutes / 60;
    }

    @Transactional
    public AttendanceEvent requestCorrection(Long eventId, String reason) {
        AttendanceEvent event = attendanceEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found"));
        if (event.isCorrection()) {
            throw new BusinessValidationException("Correction already requested");
        }
        event.setCorrection(true);
        event.setCorrectionReason(reason);
        event.setStatus(EventStatus.CORRECTION_PENDING);
        return attendanceEventRepository.save(event);
    }

    @Transactional
    public AttendanceEvent approveCorrection(Long eventId, Employee approver) {
        AttendanceEvent event = attendanceEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found"));
        if (!event.isCorrection() || event.getStatus() != EventStatus.CORRECTION_PENDING) {
            throw new BusinessValidationException("No correction pending");
        }
        event.setStatus(EventStatus.CORRECTION_APPROVED);
        event.setApprovedBy(approver);
        event.setApprovedAt(LocalDateTime.now());
        return attendanceEventRepository.save(event);
    }
}
