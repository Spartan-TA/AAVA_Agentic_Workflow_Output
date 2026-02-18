package com.companyname.wem.employee.mapper;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.dto.EmployeeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeDTO dto);
    EmployeeDTO toDto(Employee entity);
    void updateEntityFromDto(EmployeeDTO dto, @MappingTarget Employee entity);
}
