package com.wms.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for shift template API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTemplateDto {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence;
    private String overtimeRule;
}
