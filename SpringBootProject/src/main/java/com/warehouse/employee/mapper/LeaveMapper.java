package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.LeaveRequestDto;
import com.warehouse.employee.model.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LeaveMapper {
    LeaveMapper INSTANCE = Mappers.getMapper(LeaveMapper.class);

    LeaveRequest toEntity(LeaveRequestDto dto);
    LeaveRequestDto toDto(LeaveRequest entity);
}
