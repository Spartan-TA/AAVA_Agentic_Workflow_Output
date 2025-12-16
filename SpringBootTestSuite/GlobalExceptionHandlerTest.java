package com.warehouse.employee.management;

import com.warehouse.employee.management.exception.GlobalExceptionHandler;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleResourceNotFoundException_ShouldReturnNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee not found");
        ResponseEntity<Object> response = handler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Employee not found"));
    }

    @Test
    public void testHandleValidationException_ShouldReturnBadRequest() {
        BindException ex = new BindException(new Object(), "employeeRequestDto");
        ex.addError(new FieldError("employeeRequestDto", "name", "must not be blank"));
        ResponseEntity<Object> response = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("must not be blank"));
    }

    @Test
    public void testHandleGenericException_ShouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<Object> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unexpected error"));
    }
}
