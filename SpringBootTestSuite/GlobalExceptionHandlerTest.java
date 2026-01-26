package com.example.warehouse.employee;

import com.example.warehouse.employee.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Test handle validation error")
    void testHandleValidationError() {
        BindException ex = new BindException(new Object(), "employeeDTO");
        ResponseEntity<Object> response = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Validation failed"));
    }

    @Test
    @DisplayName("Test handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid argument"));
    }

    @Test
    @DisplayName("Test handle generic exception")
    void testHandleGenericException() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<Object> response = handler.handleException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Something went wrong"));
    }
}