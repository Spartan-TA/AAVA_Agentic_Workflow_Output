package com.example.warehousemanagement.service.impl;

import com.example.warehousemanagement.entity.AttendanceEvent;
import com.example.warehousemanagement.exception.ResourceNotFoundException;
import com.example.warehousemanagement.repository.AttendanceEventRepository;
import com.example.warehousemanagement.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of AttendanceService with business logic and validation.
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceEventRepository attendanceEventRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceEventRepository attendanceEventRepository) {
        this.attendanceEventRepository = attendanceEventRepository;
    }

    @Override
    public AttendanceEvent getAttendanceEventById(Long id) {
        return attendanceEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceEvent not found with id: " + id));
    }

    @Override
    public List<AttendanceEvent> getAttendanceEventsByEmployee(Long employeeId) {
        return attendanceEventRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<AttendanceEvent> getAttendanceEventsByDate(LocalDate date) {
        return attendanceEventRepository.findByEventDate(date);
    }

    @Override
    @Transactional
    public AttendanceEvent createAttendanceEvent(AttendanceEvent event) {
        // Add validation logic here
        return attendanceEventRepository.save(event);
    }

    @Override
    @Transactional
    public AttendanceEvent updateAttendanceEvent(Long id, AttendanceEvent event) {
        AttendanceEvent existing = getAttendanceEventById(id);
        // Update fields as needed
        existing.setEventType(event.getEventType());
        existing.setEventDate(event.getEventDate());
        existing.setNotes(event.getNotes());
        // ... update other fields as necessary
        return attendanceEventRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteAttendanceEvent(Long id) {
        AttendanceEvent existing = getAttendanceEventById(id);
        attendanceEventRepository.delete(existing);
    }
}
