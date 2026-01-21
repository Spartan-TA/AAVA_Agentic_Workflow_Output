package com.wms.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for GlobalExceptionHandler covering exception handling for all mapped exceptions.
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleResourceNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    public void testHandleValidation_Returns400() {
        ValidationException ex = new ValidationException("Validation failed");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    public void testHandleGenericException_Returns500() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    public void testHandleResourceNotFound_NullMessage_Returns404WithNullMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    public void testHandleValidation_NullMessage_Returns400WithNullMessage() {
        ValidationException ex = new ValidationException(null);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    public void testHandleGenericException_NullMessage_Returns500WithNullMessage() {
        Exception ex = new Exception();
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    public void testHandleResourceNotFound_CustomErrorResponse() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Custom error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);
        assertEquals("Custom error", response.getBody().getMessage());
    }

    @Test
    public void testHandleValidation_CustomErrorResponse() {
        ValidationException ex = new ValidationException("Custom validation error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex);
        assertEquals("Custom validation error", response.getBody().getMessage());
    }

    @Test
    public void testHandleGenericException_CustomErrorResponse() {
        Exception ex = new Exception("Custom generic error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        assertEquals("Custom generic error", response.getBody().getMessage());
    }

    @Test
    public void testHandleResourceNotFound_ErrorResponseFields() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Not found", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void testHandleValidation_ErrorResponseFields() {
        ValidationException ex = new ValidationException("Invalid");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex);
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Invalid", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void testHandleGenericException_ErrorResponseFields() {
        Exception ex = new Exception("Error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);
        ErrorResponse error = response.getBody();
        assertNotNull(error);
        assertEquals("Error", error.getMessage());
        assertNotNull(error.getTimestamp());
    }
}
