package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 50)
    private String channel;

    @NotBlank
    @Size(max = 1000)
    private String message;

    @NotNull
    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @NotBlank
    @Size(max = 50)
    private String deliveryStatus;
}