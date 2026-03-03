package com.warehouse.employeemgmt.common.exception;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiException and ResourceNotFoundException.
 */
class ExceptionTest {
    @Test
    @DisplayName("ApiException stores and returns correct message")
    void apiException_message() {
        ApiException ex = new ApiException("Test error message");
        assertEquals("Test error message", ex.getMessage());
    }

    @Test
    @DisplayName("ResourceNotFoundException formats message correctly")
    void resourceNotFoundException_messageFormat() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee", 42);
        assertEquals("Employee not found with id: 42", ex.getMessage());
    }

    @Test
    @DisplayName("ResourceNotFoundException is instance of ApiException")
    void resourceNotFoundException_isInstanceOfApiException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee", 1);
        assertTrue(ex instanceof ApiException);
    }
}
