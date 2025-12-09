package com.warehouse.ems.dto;

import com.warehouse.ems.domain.AttendanceRecord;
import com.warehouse.ems.domain.Employee;
import com.warehouse.ems.domain.ShiftTemplate;
import java.time.LocalDateTime;

/**
 * DTO for AttendanceRecord requests and responses.
 */
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Long shiftId;
    private boolean correctionRequested;

    // Getters and setters omitted for brevity

    /**
     * Convert DTO to AttendanceRecord entity.
     */
    public AttendanceRecord toEntity(Employee employee, ShiftTemplate shift) {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(this.id);
        record.setEmployee(employee);
        record.setClockIn(this.clockIn);
        record.setClockOut(this.clockOut);
        record.setShift(shift);
        record.setCorrectionRequested(this.correctionRequested);
        return record;
    }

    /**
     * Create DTO from AttendanceRecord entity.
     */
    public static AttendanceDto fromEntity(AttendanceRecord record) {
        AttendanceDto dto = new AttendanceDto();
        dto.id = record.getId();
        dto.employeeId = record.getEmployee().getId();
        dto.clockIn = record.getClockIn();
        dto.clockOut = record.getClockOut();
        dto.shiftId = record.getShift() != null ? record.getShift().getId() : null;
        dto.correctionRequested = record.isCorrectionRequested();
        return dto;
    }
}
