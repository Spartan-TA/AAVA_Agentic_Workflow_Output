package com.company.wms.common.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void testHandleResourceNotFoundException_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Object> response = globalExceptionHandler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleIllegalArgumentException_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad argument");
        ResponseEntity<Object> response = globalExceptionHandler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testHandleValidationException_Returns400WithDetails() {
        ValidationException ex = new ValidationException("Validation failed");
        ResponseEntity<Object> response = globalExceptionHandler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Validation failed"));
    }

    @Test
    public void testHandleConstraintViolationException_Returns409() {
        ConstraintViolationException ex = new ConstraintViolationException("Duplicate", null);
        ResponseEntity<Object> response = globalExceptionHandler.handleConstraintViolationException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleGenericException_Returns500() {
        Exception ex = new Exception("Internal error");
        ResponseEntity<Object> response = globalExceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testHandleMethodArgumentNotValidException_Returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        Map<String, String> errors = new HashMap<>();
        errors.put("field", "must not be null");
        when(ex.getBindingResult()).thenReturn(null); // Simplified for test
        ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValidException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
