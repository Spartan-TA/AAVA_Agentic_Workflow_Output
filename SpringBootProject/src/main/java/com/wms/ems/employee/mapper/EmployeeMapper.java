package com.wms.ems.employee.mapper;

import com.wms.ems.employee.entity.Employee;
import com.wms.ems.employee.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for Employee entity and DTO.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    EmployeeDto toDto(Employee employee);
    Employee toEntity(EmployeeDto dto);
    List<EmployeeDto> toDtoList(List<Employee> employees);
}
