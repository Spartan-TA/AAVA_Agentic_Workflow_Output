package com.warehouse.ems.employee;

import org.mapstruct.*;

/**
 * MapStruct mapper for Employee and DTOs.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeRequestDTO dto);
    EmployeeResponseDTO toResponseDTO(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(EmployeeRequestDTO dto, @MappingTarget Employee employee);
}
