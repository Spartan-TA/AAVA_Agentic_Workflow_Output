package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.AssetDto;
import com.warehouse.employee.model.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

    Asset toEntity(AssetDto dto);
    AssetDto toDto(Asset entity);
}
