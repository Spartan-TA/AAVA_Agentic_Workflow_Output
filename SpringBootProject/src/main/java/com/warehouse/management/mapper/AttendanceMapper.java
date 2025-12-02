package com.warehouse.management.mapper;

import com.warehouse.management.dto.AttendanceDTO;
import com.warehouse.management.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);

    AttendanceDTO toDTO(Attendance attendance);
    Attendance toEntity(AttendanceDTO attendanceDTO);
}
