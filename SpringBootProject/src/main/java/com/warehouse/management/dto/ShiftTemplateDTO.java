package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplateDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> daysOfWeek;

    @NotBlank
    @Size(max = 50)
    private String recurrence;
}