package com.example.warehouse.attendance.service;

import com.example.warehouse.attendance.entity.AttendanceEvent;
import com.example.warehouse.attendance.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    // Get all attendance events
    public List<AttendanceEvent> getAllEvents() {
        return attendanceRepository.findAll();
    }

    // Get attendance events for an employee
    public List<AttendanceEvent> getEventsByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    // Get attendance events for an employee between two dates
    public List<AttendanceEvent> getEventsByEmployeeAndDateRange(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByEmployeeIdAndEventTimeBetween(employeeId, start, end);
    }

    // Get event by ID
    public Optional<AttendanceEvent> getEventById(Long id) {
        return attendanceRepository.findById(id);
    }

    // Create new attendance event
    @Transactional
    public AttendanceEvent createEvent(AttendanceEvent event) {
        return attendanceRepository.save(event);
    }

    // Delete attendance event
    @Transactional
    public boolean deleteEvent(Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
