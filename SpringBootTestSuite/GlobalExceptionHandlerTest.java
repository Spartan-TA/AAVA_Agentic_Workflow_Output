package com.warehouse.ems.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Not found");
        ResponseEntity<?> response = handler.handleEntityNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(404, error.getStatus());
        assertEquals("Resource Not Found", error.getError());
        assertEquals("Not found", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        ResponseEntity<?> response = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(403, error.getStatus());
        assertEquals("Access Denied", error.getError());
        assertEquals("You do not have permission to access this resource", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testHandleValidation() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "badgeId", "Badge ID is required"));
        bindingResult.addError(new FieldError("object", "name", "Name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = response.getBody();
        assertEquals(2, errors.size());
        assertEquals("Badge ID is required", errors.get("badgeId"));
        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<?> response = handler.handleIllegalArgument(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(400, error.getStatus());
        assertEquals("Invalid Request", error.getError());
        assertEquals("Invalid argument", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testHandleGeneric() {
        Exception ex = new Exception("Unexpected");
        ResponseEntity<?> response = handler.handleGeneric(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(500, error.getStatus());
        assertEquals("Internal Server Error", error.getError());
        assertEquals("An unexpected error occurred. Please contact support.", error.getMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void testHandleValidation_EmptyErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = response.getBody();
        assertTrue(errors.isEmpty());
    }

    @Test
    void testHandleIllegalArgument_NullMessage() {
        IllegalArgumentException ex = new IllegalArgumentException();
        ResponseEntity<?> response = handler.handleIllegalArgument(ex);
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertNull(error.getMessage());
    }

    @Test
    void testHandleEntityNotFound_NullMessage() {
        EntityNotFoundException ex = new EntityNotFoundException();
        ResponseEntity<?> response = handler.handleEntityNotFound(ex);
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertNull(error.getMessage());
    }

    @Test
    void testHandleAccessDenied_NullMessage() {
        AccessDeniedException ex = new AccessDeniedException("");
        ResponseEntity<?> response = handler.handleAccessDenied(ex);
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("You do not have permission to access this resource", error.getMessage());
    }

    @Test
    void testHandleGeneric_NullException() {
        ResponseEntity<?> response = handler.handleGeneric(new Exception());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("An unexpected error occurred. Please contact support.", error.getMessage());
    }

    @Test
    void testErrorResponseSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse error = new ErrorResponse(400, "Bad Request", "msg", now);
        error.setStatus(401);
        error.setError("Unauthorized");
        error.setMessage("new msg");
        error.setTimestamp(now.plusSeconds(1));
        assertEquals(401, error.getStatus());
        assertEquals("Unauthorized", error.getError());
        assertEquals("new msg", error.getMessage());
        assertEquals(now.plusSeconds(1), error.getTimestamp());
    }
}
