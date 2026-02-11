package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.EmployeeRequest;
import com.warehouse.employee.dto.EmployeeResponse;
import com.warehouse.employee.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    Employee toEntity(EmployeeRequest dto);
    EmployeeResponse toDto(Employee entity);
}
