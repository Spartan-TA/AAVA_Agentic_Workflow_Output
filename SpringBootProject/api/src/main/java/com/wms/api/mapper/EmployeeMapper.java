package com.wms.api.mapper;

import com.wms.api.dto.EmployeeDto;
import com.wms.core.domain.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
    Employee toEntity(EmployeeDto dto);
}