package com.warehouse.employee.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @Schema(description = "Timestamp of the error", example = "2024-06-01T12:00:00")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    @JsonProperty("status")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    @JsonProperty("error")
    private String error;

    @Schema(description = "Error message", example = "Validation failed")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/employees")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Detailed error messages")
    @JsonProperty("details")
    private List<String> details;
}
