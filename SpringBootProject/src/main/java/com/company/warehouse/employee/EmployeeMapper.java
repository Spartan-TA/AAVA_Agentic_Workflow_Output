package com.company.warehouse.employee;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * MapStruct mapper for Employee <-> EmployeeDTO.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);
    EmployeeDTO toDto(Employee employee);
    Employee toEntity(EmployeeDTO dto);
    List<EmployeeDTO> toDtoList(List<Employee> employees);
}
