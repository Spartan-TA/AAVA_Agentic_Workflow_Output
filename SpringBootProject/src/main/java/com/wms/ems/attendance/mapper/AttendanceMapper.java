package com.wms.ems.attendance.mapper;

import com.wms.ems.attendance.model.AttendanceEvent;
import com.wms.ems.attendance.dto.AttendanceDTO;

public class AttendanceMapper {
    public static AttendanceDTO toDTO(AttendanceEvent event) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(event.getId());
        dto.setEmployeeId(event.getEmployeeId());
        dto.setType(event.getType().name());
        dto.setTimestamp(event.getTimestamp());
        dto.setDeviceId(event.getDeviceId());
        dto.setLocation(event.getLocation());
        dto.setShiftId(event.getShiftId());
        dto.setStatus(event.getStatus().name());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        return dto;
    }

    public static AttendanceEvent toEntity(AttendanceDTO dto) {
        AttendanceEvent event = new AttendanceEvent();
        event.setId(dto.getId());
        event.setEmployeeId(dto.getEmployeeId());
        event.setType(AttendanceEvent.AttendanceType.valueOf(dto.getType()));
        event.setTimestamp(dto.getTimestamp());
        event.setDeviceId(dto.getDeviceId());
        event.setLocation(dto.getLocation());
        event.setShiftId(dto.getShiftId());
        event.setStatus(AttendanceEvent.AttendanceStatus.valueOf(dto.getStatus()));
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        return event;
    }
}