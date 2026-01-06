package com.companyname.warehouse.attendance.mapper;

import com.companyname.warehouse.attendance.dto.AttendanceRequestDTO;
import com.companyname.warehouse.attendance.dto.AttendanceResponseDTO;
import com.companyname.warehouse.attendance.entity.Attendance;
import org.mapstruct.*;

/**
 * MapStruct mapper for Attendance entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    @Mapping(target = "employee.id", source = "employeeId")
    Attendance toEntity(AttendanceRequestDTO dto);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(attendance.getEmployee().getFirstName() + ' ' + attendance.getEmployee().getLastName())")
    AttendanceResponseDTO toDto(Attendance attendance);
}
