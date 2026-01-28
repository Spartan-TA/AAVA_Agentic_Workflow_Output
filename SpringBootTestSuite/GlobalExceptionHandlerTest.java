package com.warehouse.employee.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNotFoundException_ShouldReturn404() {
        NotFoundException ex = new NotFoundException("Not found");
        ResponseEntity<Map<String, String>> response = handler.handleNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("error"));
    }

    @Test
    void testHandleDuplicateException_ShouldReturn409() {
        DuplicateException ex = new DuplicateException("Duplicate");
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate", response.getBody().get("error"));
    }

    @Test
    void testHandleValidationException_ShouldReturn400WithFieldErrors() throws Exception {
        // Simulate a MethodArgumentNotValidException with field errors
        class Dummy {
            public void dummyMethod(String name) {}
        }
        Method method = Dummy.class.getMethod("dummyMethod", String.class);
        BindException bindException = new BindException(new Object(), "dummy");
        bindException.addError(new FieldError("dummy", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindException.getBindingResult());
        ResponseEntity<Map<String, String>> response = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("name"));
        assertEquals("must not be blank", response.getBody().get("name"));
    }

    @Test
    void testHandleGenericException_ShouldReturn500() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }
}
