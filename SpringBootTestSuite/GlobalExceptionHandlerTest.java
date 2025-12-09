// GlobalExceptionHandlerTest.java
package com.warehouse.ems.exception;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @AfterEach
    void tearDown() {
        // No resources to clean up
    }

    @Test
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Employee not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatusCode());
    }

    @Test
    void testHandleDuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("Employee with email already exists");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Employee with email already exists", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatusCode());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatusCode());
    }
}