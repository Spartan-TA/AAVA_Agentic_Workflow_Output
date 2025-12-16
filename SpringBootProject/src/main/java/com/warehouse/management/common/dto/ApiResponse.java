package com.warehouse.management.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Standard API response wrapper.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    @Schema(example = "true")
    private boolean success;
    @Schema(example = "Operation completed successfully.")
    private String message;
    private T data;
}
