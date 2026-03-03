package com.wms.ems.attendance.service;

import com.wms.ems.attendance.repository.AttendanceEventRepository;
import com.wms.ems.attendance.dto.ClockInDto;
import com.wms.ems.attendance.dto.ClockOutDto;
import com.wms.ems.attendance.entity.AttendanceEvent;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Attendance business logic and operations.
 */
@Slf4j
@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceEventRepository attendanceEventRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Clocks in an employee after validation.
     * @param dto ClockInDto
     * @return AttendanceEvent
     */
    public AttendanceEvent clockIn(ClockInDto dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));
        boolean hasOpen = attendanceEventRepository.existsOpenClockIn(employee.getId());
        if (hasOpen) {
            log.warn("Employee {} already has an open clock-in", employee.getId());
            throw new ValidationException("Employee already clocked in");
        }
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setClockIn(dto.getClockIn());
        event.setOpen(true);
        return attendanceEventRepository.save(event);
    }

    /**
     * Clocks out an employee after validation and calculates hours worked.
     * @param dto ClockOutDto
     * @return AttendanceEvent
     */
    public AttendanceEvent clockOut(ClockOutDto dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));
        AttendanceEvent event = attendanceEventRepository.findOpenClockInByEmployee(employee.getId())
                .orElseThrow(() -> new ValidationException("No open clock-in found for employee"));
        event.setClockOut(dto.getClockOut());
        event.setOpen(false);
        event.setHoursWorked(calculateHoursWorked(event.getClockIn(), event.getClockOut()));
        return attendanceEventRepository.save(event);
    }

    /**
     * Gets attendance events for an employee within a date range.
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List<AttendanceEvent>
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceEventRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Requests correction for an attendance event.
     * @param eventId Event ID
     * @param reason Reason for correction
     * @return AttendanceEvent
     */
    public AttendanceEvent requestCorrection(Long eventId, String reason) {
        AttendanceEvent event = attendanceEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance event not found: " + eventId));
        event.setCorrectionRequested(true);
        event.setCorrectionReason(reason);
        return attendanceEventRepository.save(event);
    }

    /**
     * Calculates hours worked between clock-in and clock-out.
     * @param clockIn LocalDateTime
     * @param clockOut LocalDateTime
     * @return Double
     */
    @Transactional(readOnly = true)
    public Double calculateHoursWorked(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockIn == null || clockOut == null) return 0.0;
        return (double) java.time.Duration.between(clockIn, clockOut).toMinutes() / 60.0;
    }
}
