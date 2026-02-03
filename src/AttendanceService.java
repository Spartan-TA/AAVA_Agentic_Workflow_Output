package com.company.wms.attendance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling attendance logic.
 */
@Service
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CorrectionRequestRepository correctionRequestRepository;

    /**
     * Clock in for an employee.
     * @param request ClockInRequest
     * @return AttendanceDTO
     */
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public AttendanceDTO clockIn(ClockInRequest request) {
        // Geofence validation can be added here
        AttendanceEvent event = new AttendanceEvent(
                request.getEmployeeId(),
                AttendanceType.CLOCK_IN,
                LocalDateTime.now(),
                request.getDeviceId(),
                request.getGeoLocation()
        );
        attendanceRepository.save(event);
        logger.info("Employee {} clocked in.", request.getEmployeeId());
        return toDTO(event);
    }

    /**
     * Clock out for an employee.
     * @param request ClockOutRequest
     * @return AttendanceDTO
     */
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public AttendanceDTO clockOut(ClockOutRequest request) {
        // Geofence validation can be added here
        AttendanceEvent event = new AttendanceEvent(
                request.getEmployeeId(),
                AttendanceType.CLOCK_OUT,
                LocalDateTime.now(),
                request.getDeviceId(),
                request.getGeoLocation()
        );
        attendanceRepository.save(event);
        logger.info("Employee {} clocked out.", request.getEmployeeId());
        return toDTO(event);
    }

    /**
     * Calculate daily totals for an employee.
     * @param employeeId Employee ID
     * @param date Date
     * @return List of AttendanceDTO
     */
    @PreAuthorize("hasRole('MANAGER') or #employeeId == authentication.principal.id")
    public List<AttendanceDTO> calculateDailyTotals(Long employeeId, LocalDate date) {
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeIdAndDate(employeeId, date);
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Request correction for an attendance event.
     * @param employeeId Employee ID
     * @param originalTimestamp Original timestamp
     * @param reason Reason for correction
     * @return CorrectionRequest
     */
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public CorrectionRequest requestCorrection(Long employeeId, LocalDateTime originalTimestamp, String reason) {
        CorrectionRequest correction = new CorrectionRequest(employeeId, originalTimestamp, reason);
        correctionRequestRepository.save(correction);
        logger.info("Correction requested by employee {} for {}.", employeeId, originalTimestamp);
        return correction;
    }

    private AttendanceDTO toDTO(AttendanceEvent event) {
        return new AttendanceDTO(
                event.getId(),
                event.getEmployeeId(),
                event.getType(),
                event.getTimestamp(),
                event.getDeviceId(),
                event.getGeoLocation(),
                event.isCorrectionRequested()
        );
    }
}
