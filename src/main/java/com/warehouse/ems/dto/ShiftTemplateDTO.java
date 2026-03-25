package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplateDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private boolean recurring;
}
