package com.wms.ems.employee.mapper;

import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mapping(target = "id", source = "id")
    EmployeeDTO toDto(Employee employee);

    Employee toEntity(EmployeeDTO employeeDTO);
}
