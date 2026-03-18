package com.company.wms.mapper;

import com.company.wms.domain.Employee;
import com.company.wms.dto.EmployeeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Employee and EmployeeDTO.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mapping(target = "roles", expression = "java(employee.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()))")
    EmployeeDTO toDto(Employee employee);

    @Mapping(target = "roles", ignore = true)
    Employee toEntity(EmployeeDTO employeeDTO);
}
