package com.companyname.warehouse.employee.mapper;

import com.companyname.warehouse.employee.dto.EmployeeRequestDTO;
import com.companyname.warehouse.employee.dto.EmployeeResponseDTO;
import com.companyname.warehouse.employee.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Employee entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeRequestDTO dto);
    EmployeeResponseDTO toDto(Employee employee);
    void updateEntityFromDto(EmployeeRequestDTO dto, @MappingTarget Employee employee);
}
