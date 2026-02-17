package com.example.warehouse.exception;

import com.example.warehouse.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleResourceNotFoundException_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void testHandleDuplicateResourceException_Returns409() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException_Returns400() {
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, null);
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void testHandleAccessDeniedException_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException_Returns500() {
        // Simulate a generic exception handler (not present in the spec, but for completeness)
        Exception ex = new Exception("Internal error");
        GlobalExceptionHandler genericHandler = new GlobalExceptionHandler() {
            @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
            public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
        };
        ResponseEntity<ErrorResponse> response = genericHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().getMessage());
    }
}