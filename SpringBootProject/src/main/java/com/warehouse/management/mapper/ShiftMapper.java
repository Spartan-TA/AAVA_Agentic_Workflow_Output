package com.warehouse.management.mapper;

import com.warehouse.management.dto.ShiftDTO;
import com.warehouse.management.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    ShiftMapper INSTANCE = Mappers.getMapper(ShiftMapper.class);

    ShiftDTO toDTO(Shift shift);
    Shift toEntity(ShiftDTO shiftDTO);
}
