package com.warehouse.ems.mapper;

import com.warehouse.ems.dto.EmployeeDTO;
import com.warehouse.ems.entity.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDto(Employee employee);
    Employee toEntity(EmployeeDTO dto);
}