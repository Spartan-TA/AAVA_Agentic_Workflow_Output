package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncidentDTO {
    private Long id;

    @NotBlank
    private String description;

    private String location;
    private String severity;

    @NotNull
    private Long reportedById;

    @NotBlank
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
