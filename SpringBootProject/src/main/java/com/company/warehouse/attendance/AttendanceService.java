package com.company.warehouse.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for attendance logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {
    private final ClockEventRepository clockEventRepository;

    public ClockEvent clockIn(ClockEventDto dto) {
        ClockEvent event = ClockEvent.builder()
                .employeeId(dto.getEmployeeId())
                .timestamp(dto.getTimestamp())
                .type(ClockEvent.ClockEventType.CLOCK_IN)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .deviceId(dto.getDeviceId())
                .build();
        return clockEventRepository.save(event);
    }

    public ClockEvent clockOut(ClockEventDto dto) {
        ClockEvent event = ClockEvent.builder()
                .employeeId(dto.getEmployeeId())
                .timestamp(dto.getTimestamp())
                .type(ClockEvent.ClockEventType.CLOCK_OUT)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .deviceId(dto.getDeviceId())
                .build();
        return clockEventRepository.save(event);
    }

    public double calculateHours(Long employeeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<ClockEvent> events = clockEventRepository.findEventsForPeriod(employeeId, start, end);
        double totalHours = 0.0;
        LocalDateTime lastIn = null;
        for (ClockEvent event : events) {
            if (event.getType() == ClockEvent.ClockEventType.CLOCK_IN) {
                lastIn = event.getTimestamp();
            } else if (event.getType() == ClockEvent.ClockEventType.CLOCK_OUT && lastIn != null) {
                totalHours += Duration.between(lastIn, event.getTimestamp()).toMinutes() / 60.0;
                lastIn = null;
            }
        }
        return totalHours;
    }
}
