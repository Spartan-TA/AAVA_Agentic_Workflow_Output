package com.company.wms.attendance;

import com.company.wms.employee.Employee;
import com.company.wms.employee.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling attendance logic: clock-in, clock-out, geofence validation, daily totals, corrections.
 */
@Service
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CorrectionRequestRepository correctionRequestRepository;

    @Value("${wms.attendance.geofence.radius:100}")
    private double geofenceRadiusMeters;

    public AttendanceService(AttendanceRepository attendanceRepository,
                            EmployeeRepository employeeRepository,
                            CorrectionRequestRepository correctionRequestRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.correctionRequestRepository = correctionRequestRepository;
    }

    /**
     * Clock in an employee.
     * @param request ClockInRequest
     * @return AttendanceDTO
     */
    @Transactional
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('ADMIN')")
    public AttendanceDTO clockIn(ClockInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        validateGeofence(request.getLatitude(), request.getLongitude());
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setType(AttendanceType.CLOCK_IN);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setGeoLocation(new GeoLocation(request.getLatitude(), request.getLongitude()));
        event.setCorrectionRequested(false);
        AttendanceEvent saved = attendanceRepository.save(event);
        logger.info("Employee {} clocked in at {}", employee.getId(), saved.getTimestamp());
        return toDTO(saved);
    }

    /**
     * Clock out an employee.
     * @param request ClockOutRequest
     * @return AttendanceDTO
     */
    @Transactional
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('ADMIN')")
    public AttendanceDTO clockOut(ClockOutRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        validateGeofence(request.getLatitude(), request.getLongitude());
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setType(AttendanceType.CLOCK_OUT);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setGeoLocation(new GeoLocation(request.getLatitude(), request.getLongitude()));
        event.setCorrectionRequested(false);
        AttendanceEvent saved = attendanceRepository.save(event);
        logger.info("Employee {} clocked out at {}", employee.getId(), saved.getTimestamp());
        return toDTO(saved);
    }

    /**
     * Validate if the location is within the allowed geofence.
     * Throws IllegalArgumentException if not.
     */
    protected void validateGeofence(Double latitude, Double longitude) {
        // For demo, always allow. In production, compare with warehouse location and radius.
        Assert.notNull(latitude, "Latitude required");
        Assert.notNull(longitude, "Longitude required");
        // TODO: Implement geofence logic
    }

    /**
     * Calculate daily totals for an employee.
     * @param employeeId Employee ID
     * @param date Date
     * @return List of AttendanceDTO
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('ADMIN')")
    public List<AttendanceDTO> calculateDailyTotals(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeAndTimestampBetween(employee, start, end);
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Request a correction for an attendance event.
     * @param correction CorrectionRequest
     * @return CorrectionRequest
     */
    @Transactional
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('ADMIN')")
    public CorrectionRequest requestCorrection(CorrectionRequest correction) {
        correction.setResolved(false);
        CorrectionRequest saved = correctionRequestRepository.save(correction);
        logger.info("Correction requested by employee {} for {}", correction.getEmployee().getId(), correction.getType());
        return saved;
    }

    private AttendanceDTO toDTO(AttendanceEvent event) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(event.getId());
        dto.setEmployeeId(event.getEmployee().getId());
        dto.setType(event.getType());
        dto.setTimestamp(event.getTimestamp());
        dto.setDeviceId(event.getDeviceId());
        if (event.getGeoLocation() != null) {
            dto.setLatitude(event.getGeoLocation().getLatitude());
            dto.setLongitude(event.getGeoLocation().getLongitude());
        }
        dto.setCorrectionRequested(event.getCorrectionRequested());
        return dto;
    }
}
