package com.wms.scheduling.dtos;

import lombok.*;
import java.time.LocalTime;

/**
 * Data Transfer Object for ShiftTemplate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplateDto {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;
}
