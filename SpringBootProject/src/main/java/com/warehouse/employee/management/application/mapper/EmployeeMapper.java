package com.warehouse.employee.management.application.mapper;

import com.warehouse.employee.management.application.dto.CreateEmployeeRequest;
import com.warehouse.employee.management.application.dto.EmployeeResponse;
import com.warehouse.employee.management.application.dto.UpdateEmployeeRequest;
import com.warehouse.employee.management.domain.employee.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(CreateEmployeeRequest request);
    Employee toEntity(UpdateEmployeeRequest request);
    EmployeeResponse toResponse(Employee employee);
}
