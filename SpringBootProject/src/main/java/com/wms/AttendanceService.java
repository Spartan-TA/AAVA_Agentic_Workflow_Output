package com.wms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Attendance business logic.
 */
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public List<AttendanceEvent> getAllEvents() {
        return attendanceRepository.findAll();
    }

    public List<AttendanceEvent> getEventsByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public Optional<AttendanceEvent> getEventById(Long id) {
        return attendanceRepository.findById(id);
    }

    @Transactional
    public AttendanceEvent createEvent(AttendanceEvent event) {
        return attendanceRepository.save(event);
    }

    @Transactional
    public AttendanceEvent updateEvent(Long id, AttendanceEvent updated) {
        AttendanceEvent event = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance event not found"));
        event.setEmployeeId(updated.getEmployeeId());
        event.setEventTime(updated.getEventTime());
        event.setEventType(updated.getEventType());
        return attendanceRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        attendanceRepository.deleteById(id);
    }
}
