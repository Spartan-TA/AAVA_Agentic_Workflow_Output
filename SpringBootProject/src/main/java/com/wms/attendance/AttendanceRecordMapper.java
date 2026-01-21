package com.wms.attendance;

/**
 * Mapper for AttendanceRecord and AttendanceRecordDTO.
 */
public class AttendanceRecordMapper {
    public static AttendanceRecordDTO toDto(AttendanceRecord record) {
        return AttendanceRecordDTO.builder()
                .id(record.getId())
                .employeeId(record.getEmployeeId())
                .clockIn(record.getClockIn())
                .clockOut(record.getClockOut())
                .shiftId(record.getShiftId())
                .deviceInfo(record.getDeviceInfo())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }

    public static AttendanceRecord toEntity(AttendanceRecordDTO dto) {
        return AttendanceRecord.builder()
                .id(dto.getId())
                .employeeId(dto.getEmployeeId())
                .clockIn(dto.getClockIn())
                .clockOut(dto.getClockOut())
                .shiftId(dto.getShiftId())
                .deviceInfo(dto.getDeviceInfo())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}