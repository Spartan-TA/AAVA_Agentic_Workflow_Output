package com.wms.ems.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for error responses in exception handling.
 * <p>
 * Contains details about the error, including timestamp, status, error type, message, and request path.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code.
     */
    private int status;

    /**
     * The error type (e.g., "Bad Request").
     */
    private String error;

    /**
     * Detailed error message.
     */
    private String message;

    /**
     * The request path where the error occurred.
     */
    private String path;
}
