package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String entityType;

    @NotNull
    private Long entityId;

    @NotBlank
    @Size(max = 50)
    private String action;

    @NotBlank
    @Size(max = 100)
    private String actor;

    @NotNull
    private LocalDateTime timestamp;

    @Size(max = 1000)
    private String beforeValue;

    @Size(max = 1000)
    private String afterValue;
}