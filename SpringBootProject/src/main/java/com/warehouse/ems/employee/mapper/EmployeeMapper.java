package com.warehouse.ems.employee.mapper;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.dto.EmployeeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDto(Employee employee);
    Employee toEntity(EmployeeDTO dto);
}