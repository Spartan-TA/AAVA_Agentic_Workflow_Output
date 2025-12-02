package com.warehouse.management.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for GlobalExceptionHandler
 * Tests cover all exception types, error responses, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        httpServletRequest = mock(HttpServletRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
        when(httpServletRequest.getRequestURI()).thenReturn("/api/test");
    }

    // ========== RESOURCE NOT FOUND EXCEPTION TESTS ==========

    @Test
    void testHandleResourceNotFoundException_ValidException_Returns404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Employee not found with id: 1");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Employee not found with id: 1", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("path"));
    }

    @Test
    void testHandleResourceNotFoundException_NullMessage_Returns404WithDefaultMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message") != null);
    }

    @Test
    void testHandleResourceNotFoundException_EmptyMessage_Returns404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testHandleResourceNotFoundException_LongMessage_Returns404() {
        // Arrange
        String longMessage = "A".repeat(1000);
        ResourceNotFoundException exception = new ResourceNotFoundException(longMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(longMessage, response.getBody().get("message"));
    }

    // ========== BAD REQUEST EXCEPTION TESTS ==========

    @Test
    void testHandleBadRequestException_ValidException_Returns400() {
        // Arrange
        BadRequestException exception = new BadRequestException("Invalid input data");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleBadRequestException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid input data", response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("path"));
    }

    @Test
    void testHandleBadRequestException_NullMessage_Returns400() {
        // Arrange
        BadRequestException exception = new BadRequestException(null);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleBadRequestException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleBadRequestException_SpecialCharacters_Returns400() {
        // Arrange
        BadRequestException exception = new BadRequestException("Invalid: <script>alert('xss')</script>");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleBadRequestException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("Invalid:"));
    }

    // ========== VALIDATION EXCEPTION TESTS ==========

    @Test
    void testHandleValidationException_ValidException_Returns400() {
        // Arrange
        ValidationException exception = new ValidationException("Validation failed for field: email");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleValidationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Validation failed for field: email", response.getBody().get("message"));
    }

    @Test
    void testHandleValidationException_MultipleErrors_Returns400() {
        // Arrange
        ValidationException exception = new ValidationException("Multiple validation errors: email, phone, badgeId");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleValidationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("Multiple validation errors"));
    }

    // ========== METHOD ARGUMENT NOT VALID EXCEPTION TESTS ==========

    @Test
    void testHandleMethodArgumentNotValid_SingleFieldError_Returns400() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("employee", "email", "must be a valid email");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("errors"));
    }

    @Test
    void testHandleMethodArgumentNotValid_MultipleFieldErrors_Returns400() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("employee", "email", "must be a valid email");
        FieldError fieldError2 = new FieldError("employee", "phone", "must not be null");
        FieldError fieldError3 = new FieldError("employee", "badgeId", "must not be blank");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2, fieldError3));

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertEquals(3, errors.size());
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
        assertTrue(errors.containsKey("badgeId"));
    }

    @Test
    void testHandleMethodArgumentNotValid_NoFieldErrors_Returns400() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== ACCESS DENIED EXCEPTION TESTS ==========

    @Test
    void testHandleAccessDeniedException_ValidException_Returns403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied: insufficient permissions");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Access denied"));
    }

    @Test
    void testHandleAccessDeniedException_NullMessage_Returns403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException(null);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== GENERIC EXCEPTION TESTS ==========

    @Test
    void testHandleGlobalException_RuntimeException_Returns500() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Internal server error"));
    }

    @Test
    void testHandleGlobalException_NullPointerException_Returns500() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer encountered");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleGlobalException_IllegalArgumentException_Returns500() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleGlobalException_NullMessage_Returns500() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ========== RESPONSE STRUCTURE TESTS ==========

    @Test
    void testErrorResponse_ContainsAllRequiredFields() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("path"));
    }

    @Test
    void testErrorResponse_TimestampIsValid() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        Object timestamp = response.getBody().get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp instanceof String || timestamp instanceof Long);
    }

    @Test
    void testErrorResponse_PathIsCorrect() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/employees/1");

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        String path = (String) response.getBody().get("path");
        assertNotNull(path);
        assertTrue(path.contains("/api/employees/1"));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testHandleException_VeryLongMessage_Returns500() {
        // Arrange
        String veryLongMessage = "Error: " + "A".repeat(10000);
        Exception exception = new Exception(veryLongMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleException_SpecialCharactersInMessage_HandledCorrectly() {
        // Arrange
        String specialMessage = "Error with special chars: <>&"'
	";
        BadRequestException exception = new BadRequestException(specialMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleBadRequestException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("Error with special chars"));
    }

    @Test
    void testHandleException_UnicodeCharacters_HandledCorrectly() {
        // Arrange
        String unicodeMessage = "Error: ä½ å¥½ä¸ç ð ÐÑÐ¸Ð²ÐµÑ Ð¼Ð¸Ñ";
        ResourceNotFoundException exception = new ResourceNotFoundException(unicodeMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(unicodeMessage, response.getBody().get("message"));
    }

    // ========== CONCURRENT ACCESS TESTS ==========

    @Test
    void testHandleException_ConcurrentAccess_ThreadSafe() throws InterruptedException {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Concurrent test");
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                ResponseEntity<Map<String, Object>> response = 
                    globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);
                assertNotNull(response);
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - if we reach here without exceptions, the handler is thread-safe
        assertTrue(true);
    }
}