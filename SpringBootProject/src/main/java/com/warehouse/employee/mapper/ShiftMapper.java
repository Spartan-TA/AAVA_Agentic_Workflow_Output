package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.ShiftAssignmentRequest;
import com.warehouse.employee.dto.ShiftAssignmentResponse;
import com.warehouse.employee.model.ShiftAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    ShiftMapper INSTANCE = Mappers.getMapper(ShiftMapper.class);

    ShiftAssignment toEntity(ShiftAssignmentRequest dto);
    ShiftAssignmentResponse toDto(ShiftAssignment entity);
}
