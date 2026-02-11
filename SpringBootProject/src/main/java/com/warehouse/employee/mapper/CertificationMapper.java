package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.CertificationDto;
import com.warehouse.employee.model.Certification;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CertificationMapper {
    CertificationMapper INSTANCE = Mappers.getMapper(CertificationMapper.class);

    Certification toEntity(CertificationDto dto);
    CertificationDto toDto(Certification entity);
}
