package com.warehouse.management.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO {
    private Long id;
    private Long employeeId;

    @NotBlank
    @Size(max = 20)
    private String eventType;

    @NotNull
    private LocalDateTime timestamp;

    @Size(max = 100)
    private String location;

    @Size(max = 100)
    private String device;
}
