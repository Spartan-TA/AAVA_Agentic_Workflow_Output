package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Attendance;
import com.warehouse.employee.management.repository.AttendanceRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Attendance entity.
 * Implements business logic, validation, exception handling, and audit logging.
 */
@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Get all active attendance records.
     */
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAllActive();
    }

    /**
     * Get attendance by ID.
     */
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));
    }

    /**
     * Create a new attendance record.
     */
    @Transactional
    public Attendance createAttendance(Attendance attendance) {
        Attendance saved = attendanceRepository.save(attendance);
        auditService.logCreate("Attendance", saved.getId(), saved);
        return saved;
    }

    /**
     * Update an existing attendance record.
     */
    @Transactional
    public Attendance updateAttendance(Long id, Attendance updatedAttendance) {
        Attendance existing = getAttendanceById(id);
        // Update fields
        existing.setDate(updatedAttendance.getDate());
        existing.setStatus(updatedAttendance.getStatus());
        // ... other fields
        Attendance saved = attendanceRepository.save(existing);
        auditService.logUpdate("Attendance", saved.getId(), saved);
        return saved;
    }

    /**
     * Soft-delete an attendance record.
     */
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance existing = getAttendanceById(id);
        existing.setDeletedAt(java.time.LocalDateTime.now());
        attendanceRepository.save(existing);
        auditService.logDelete("Attendance", existing.getId());
    }
}
