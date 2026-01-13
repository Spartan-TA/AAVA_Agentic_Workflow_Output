package com.wms.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for shift assignment API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentDto {
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
}
