package com.warehouse.employee.service;

import com.warehouse.employee.domain.AttendanceEvent;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.AttendanceResponse;
import com.warehouse.employee.dto.ClockInRequest;
import com.warehouse.employee.exception.AttendanceException;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.AttendanceMapper;
import com.warehouse.employee.repository.AttendanceEventRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for attendance clock-in/out, geofence validation, and hours calculation.
 */
@Service
public class AttendanceService {

    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    // Example geofence coordinates (latitude, longitude, radius in meters)
    private static final double GEOFENCE_LAT = 40.7128;
    private static final double GEOFENCE_LON = -74.0060;
    private static final double GEOFENCE_RADIUS_METERS = 200.0;

    @Autowired
    public AttendanceService(AttendanceEventRepository attendanceEventRepository,
                            EmployeeRepository employeeRepository,
                            AttendanceMapper attendanceMapper) {
        this.attendanceEventRepository = attendanceEventRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceMapper = attendanceMapper;
    }

    /**
     * Clock in for an employee with geofence validation.
     * @param request ClockInRequest
     * @return AttendanceResponse
     */
    @Transactional
    public AttendanceResponse clockIn(ClockInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + request.getEmployeeId()));
        if (!isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new AttendanceException("Clock-in location is outside allowed geofence.");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setEventType("CLOCK_IN");
        event.setEventTime(LocalDateTime.now());
        event.setLocation(request.getLocation());
        event.setDeviceInfo(request.getDeviceInfo());
        AttendanceEvent saved = attendanceEventRepository.save(event);
        return attendanceMapper.toResponse(saved);
    }

    /**
     * Clock out for an employee with geofence validation.
     * @param request ClockInRequest
     * @return AttendanceResponse
     */
    @Transactional
    public AttendanceResponse clockOut(ClockInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + request.getEmployeeId()));
        if (!isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new AttendanceException("Clock-out location is outside allowed geofence.");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setEventType("CLOCK_OUT");
        event.setEventTime(LocalDateTime.now());
        event.setLocation(request.getLocation());
        event.setDeviceInfo(request.getDeviceInfo());
        AttendanceEvent saved = attendanceEventRepository.save(event);
        return attendanceMapper.toResponse(saved);
    }

    /**
     * Calculate daily hours worked for an employee.
     * @param employeeId Employee ID
     * @param date LocalDate
     * @return double hours worked
     */
    @Transactional(readOnly = true)
    public double calculateDailyHours(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<AttendanceEvent> events = attendanceEventRepository.findByEmployeeAndEventTimeBetween(employee, start, end);
        events.sort((a, b) -> a.getEventTime().compareTo(b.getEventTime()));
        double totalMinutes = 0;
        LocalDateTime lastClockIn = null;
        for (AttendanceEvent event : events) {
            if ("CLOCK_IN".equals(event.getEventType())) {
                lastClockIn = event.getEventTime();
            } else if ("CLOCK_OUT".equals(event.getEventType()) && lastClockIn != null) {
                totalMinutes += Duration.between(lastClockIn, event.getEventTime()).toMinutes();
                lastClockIn = null;
            }
        }
        return totalMinutes / 60.0;
    }

    /**
     * Geofence validation using Haversine formula.
     */
    private boolean isWithinGeofence(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return false;
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(latitude - GEOFENCE_LAT);
        double dLon = Math.toRadians(longitude - GEOFENCE_LON);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(GEOFENCE_LAT)) * Math.cos(Math.toRadians(latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return distance <= GEOFENCE_RADIUS_METERS;
    }
}
