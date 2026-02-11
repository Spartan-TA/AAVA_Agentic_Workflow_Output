package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.AttendanceResponse;
import com.warehouse.employee.model.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceMapper INSTANCE = Mappers.getMapper(AttendanceMapper.class);

    AttendanceResponse toDto(Attendance entity);
}
