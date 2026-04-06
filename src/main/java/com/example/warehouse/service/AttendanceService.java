package com.example.warehouse.service;

import com.example.warehouse.dto.ClockInRequestDTO;
import com.example.warehouse.dto.ClockOutRequestDTO;
import com.example.warehouse.dto.AttendanceDTO;
import com.example.warehouse.entity.Attendance;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.CertificationExpiredException;
import com.example.warehouse.repository.AttendanceRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    @Value("${attendance.geofence.radius:100}")
    private double geofenceRadiusMeters;
    @Value("${attendance.geofence.lat}")
    private double geofenceLat;
    @Value("${attendance.geofence.lng}")
    private double geofenceLng;

    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public AttendanceDTO clockIn(Long employeeId, ClockInRequestDTO request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new IllegalArgumentException("Clock-in outside geofence");
        }
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setClockInTime(LocalDateTime.now());
        attendance.setClockInDeviceId(request.getDeviceId());
        attendance.setClockInLatitude(request.getLatitude());
        attendance.setClockInLongitude(request.getLongitude());
        attendance.setMissedPunch(false);
        attendanceRepository.save(attendance);
        return AttendanceDTO.from(attendance);
    }

    @Transactional
    public AttendanceDTO clockOut(Long employeeId, ClockOutRequestDTO request) {
        Attendance attendance = attendanceRepository.findTopByEmployeeIdAndClockOutTimeIsNullOrderByClockInTimeDesc(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active clock-in found"));
        if (!isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new IllegalArgumentException("Clock-out outside geofence");
        }
        attendance.setClockOutTime(LocalDateTime.now());
        attendance.setClockOutDeviceId(request.getDeviceId());
        attendance.setClockOutLatitude(request.getLatitude());
        attendance.setClockOutLongitude(request.getLongitude());
        attendance.setHoursWorked(Duration.between(attendance.getClockInTime(), attendance.getClockOutTime()).toHours());
        attendanceRepository.save(attendance);
        return AttendanceDTO.from(attendance);
    }

    public List<AttendanceDTO> getAttendanceReport(Long supervisorId, LocalDateTime from, LocalDateTime to) {
        // RBAC and filtering logic omitted for brevity
        List<Attendance> records = attendanceRepository.findByClockInTimeBetween(from, to);
        return records.stream().map(AttendanceDTO::from).toList();
    }

    public void handleMissedPunch(Long employeeId) {
        // Workflow for missed punch
        Attendance attendance = attendanceRepository.findTopByEmployeeIdAndClockOutTimeIsNullOrderByClockInTimeDesc(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active clock-in found"));
        attendance.setMissedPunch(true);
        attendanceRepository.save(attendance);
        // Notify supervisor, etc.
    }

    private boolean isWithinGeofence(double lat, double lng) {
        double distance = haversine(lat, lng, geofenceLat, geofenceLng);
        return distance <= geofenceRadiusMeters;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
