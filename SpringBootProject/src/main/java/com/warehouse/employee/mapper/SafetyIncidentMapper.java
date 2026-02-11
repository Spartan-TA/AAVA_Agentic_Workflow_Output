package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.SafetyIncidentDto;
import com.warehouse.employee.model.SafetyIncident;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SafetyIncidentMapper {
    SafetyIncidentMapper INSTANCE = Mappers.getMapper(SafetyIncidentMapper.class);

    SafetyIncident toEntity(SafetyIncidentDto dto);
    SafetyIncidentDto toDto(SafetyIncident entity);
}
