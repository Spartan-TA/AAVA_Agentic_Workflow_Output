package com.companyname.wem.employee.mapper;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.dto.EmployeeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    EmployeeDTO toDto(Employee employee);
    Employee toEntity(EmployeeDTO employeeDTO);
}
