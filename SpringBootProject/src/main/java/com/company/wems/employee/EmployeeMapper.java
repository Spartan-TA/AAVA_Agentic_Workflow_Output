package com.company.wems.employee;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
    Employee toEntity(CreateEmployeeRequest request);
    void updateEntityFromDto(UpdateEmployeeRequest request, @MappingTarget Employee employee);
}