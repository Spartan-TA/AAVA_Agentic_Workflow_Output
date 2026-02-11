package com.warehouse.employee.mapper;

import com.warehouse.employee.dto.PerformanceReviewDto;
import com.warehouse.employee.model.PerformanceReview;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PerformanceReviewMapper {
    PerformanceReviewMapper INSTANCE = Mappers.getMapper(PerformanceReviewMapper.class);

    PerformanceReview toEntity(PerformanceReviewDto dto);
    PerformanceReviewDto toDto(PerformanceReview entity);
}
