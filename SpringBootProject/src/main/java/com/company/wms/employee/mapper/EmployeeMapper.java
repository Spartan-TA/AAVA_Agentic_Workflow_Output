package com.company.wms.employee.mapper;

import com.company.wms.employee.entity.Employee;
import com.company.wms.employee.dto.EmployeeDTO;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Employee entity and DTO.
 * Automatically generates implementation at compile time.
 * 
 * @author WMS Development Team
 * @version 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {
    
    /**
     * Convert Employee entity to DTO
     * @param employee source entity
     * @return EmployeeDTO
     */
    EmployeeDTO toDto(Employee employee);
    
    /**
     * Convert EmployeeDTO to entity
     * @param dto source DTO
     * @return Employee entity
     */
    @Mapping(target = "deleted", constant = "false")
    Employee toEntity(EmployeeDTO dto);
    
    /**
     * Convert list of Employee entities to DTOs
     * @param employees list of entities
     * @return list of DTOs
     */
    List<EmployeeDTO> toDtoList(List<Employee> employees);
    
    /**
     * Update existing Employee entity from DTO
     * @param dto source DTO
     * @param employee target entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "badgeId", ignore = true)
    void updateEntityFromDto(EmployeeDTO dto, @MappingTarget Employee employee);
}