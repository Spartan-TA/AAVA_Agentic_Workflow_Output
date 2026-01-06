package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncidentDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long reporterId;

    @NotBlank
    @Size(max = 50)
    private String severity;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    @Size(min = 1)
    private List<@NotNull Long> involvedEmployees;

    @NotBlank
    @Size(max = 50)
    private String status;

    @Size(max = 1000)
    private String investigationNotes;

    @Size(max = 1000)
    private String correctiveActions;
}