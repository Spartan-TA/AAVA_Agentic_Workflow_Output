package com.warehouse.ems.attendance;

import com.warehouse.ems.common.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Attendance logic: clock-in/out, hours calculation, corrections.
 */
@Service
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Employee clocks in. Creates a new AttendanceEvent with clock-in time.
     * @param employeeId Employee ID
     * @return AttendanceEvent
     */
    @Transactional
    public AttendanceEvent clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        // Prevent multiple clock-ins per day
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, today, today);
        if (!events.isEmpty() && events.get(0).getClockIn() != null) {
            logger.warn("Employee {} already clocked in today.", employeeId);
            throw new IllegalStateException("Already clocked in today");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setEventDate(today);
        event.setClockIn(LocalDateTime.now());
        logger.info("Employee {} clocked in at {}", employeeId, event.getClockIn());
        return attendanceRepository.save(event);
    }

    /**
     * Employee clocks out. Updates the existing AttendanceEvent with clock-out time.
     * @param employeeId Employee ID
     * @return AttendanceEvent
     */
    @Transactional
    public AttendanceEvent clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, today, today);
        if (events.isEmpty()) {
            logger.warn("No clock-in found for employee {} today.", employeeId);
            throw new ResourceNotFoundException("No clock-in found for today");
        }
        AttendanceEvent event = events.get(0);
        if (event.getClockOut() != null) {
            logger.warn("Employee {} already clocked out today.", employeeId);
            throw new IllegalStateException("Already clocked out today");
        }
        event.setClockOut(LocalDateTime.now());
        logger.info("Employee {} clocked out at {}", employeeId, event.getClockOut());
        return attendanceRepository.save(event);
    }

    /**
     * Calculate total hours worked for an employee in a date range.
     * @param employeeId Employee ID
     * @param start Start date
     * @param end End date
     * @return Total hours
     */
    public double calculateHours(Long employeeId, LocalDate start, LocalDate end) {
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
        double totalHours = 0.0;
        for (AttendanceEvent event : events) {
            if (event.getClockIn() != null && event.getClockOut() != null) {
                Duration duration = Duration.between(event.getClockIn(), event.getClockOut());
                totalHours += duration.toMinutes() / 60.0;
            }
        }
        logger.info("Employee {} worked {} hours between {} and {}", employeeId, totalHours, start, end);
        return totalHours;
    }

    /**
     * Submit a correction for an attendance event.
     * @param eventId AttendanceEvent ID
     * @param newClockIn New clock-in time
     * @param newClockOut New clock-out time
     * @return Updated AttendanceEvent
     */
    @Transactional
    public AttendanceEvent submitCorrection(Long eventId, LocalDateTime newClockIn, LocalDateTime newClockOut) {
        Optional<AttendanceEvent> optional = attendanceRepository.findById(eventId);
        if (optional.isEmpty()) {
            logger.error("Attendance event {} not found for correction.", eventId);
            throw new ResourceNotFoundException("Attendance event not found");
        }
        AttendanceEvent event = optional.get();
        event.setClockIn(newClockIn);
        event.setClockOut(newClockOut);
        event.setCorrectionRequested(true);
        logger.info("Correction submitted for event {}: clockIn={}, clockOut={}", eventId, newClockIn, newClockOut);
        return attendanceRepository.save(event);
    }

    /**
     * Approve a correction for an attendance event.
     * @param eventId AttendanceEvent ID
     * @return Updated AttendanceEvent
     */
    @Transactional
    public AttendanceEvent approveCorrection(Long eventId) {
        Optional<AttendanceEvent> optional = attendanceRepository.findById(eventId);
        if (optional.isEmpty()) {
            logger.error("Attendance event {} not found for approval.", eventId);
            throw new ResourceNotFoundException("Attendance event not found");
        }
        AttendanceEvent event = optional.get();
        event.setCorrectionRequested(false);
        event.setCorrectionApproved(true);
        logger.info("Correction approved for event {}", eventId);
        return attendanceRepository.save(event);
    }

    /**
     * Get attendance events for reporting.
     * @param employeeId Employee ID
     * @param start Start date
     * @param end End date
     * @return List of AttendanceEvent
     */
    public List<AttendanceEvent> getAttendanceReport(Long employeeId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
    }
}
