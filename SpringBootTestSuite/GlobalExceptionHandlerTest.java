package com.company.warehouse.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for GlobalExceptionHandler
 * Covers all exception handling scenarios
 */
@DisplayName("Global Exception Handler Tests")
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ========== ILLEGAL ARGUMENT EXCEPTION TESTS ==========

    @Test
    @DisplayName("Test handle IllegalArgumentException returns 400 Bad Request")
    public void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Test handle IllegalArgumentException with null message")
    public void testHandleIllegalArgumentExceptionWithNullMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle IllegalArgumentException with empty message")
    public void testHandleIllegalArgumentExceptionWithEmptyMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle IllegalArgumentException with long message")
    public void testHandleIllegalArgumentExceptionWithLongMessage() {
        // Arrange
        String longMessage = "Error: " + "a".repeat(500);
        IllegalArgumentException exception = new IllegalArgumentException(longMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(longMessage, response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle IllegalArgumentException with special characters")
    public void testHandleIllegalArgumentExceptionWithSpecialCharacters() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Error: <script>alert('xss')</script>");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("<script>"));
    }

    // ========== ACCESS DENIED EXCEPTION TESTS ==========

    @Test
    @DisplayName("Test handle AccessDeniedException returns 403 Forbidden")
    public void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Test handle AccessDeniedException with null message")
    public void testHandleAccessDeniedExceptionWithNullMessage() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException(null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle AccessDeniedException with custom message")
    public void testHandleAccessDeniedExceptionWithCustomMessage() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Insufficient permissions");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    // ========== METHOD ARGUMENT NOT VALID EXCEPTION TESTS ==========

    @Test
    @DisplayName("Test handle MethodArgumentNotValidException returns 400 with field errors")
    public void testHandleMethodArgumentNotValidException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("employee", "name", "Name is required");
        FieldError fieldError2 = new FieldError("employee", "badgeId", "Badge ID must be unique");
        
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Name is required", response.getBody().get("name"));
        assertEquals("Badge ID must be unique", response.getBody().get("badgeId"));
    }

    @Test
    @DisplayName("Test handle MethodArgumentNotValidException with single field error")
    public void testHandleMethodArgumentNotValidExceptionWithSingleError() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("employee", "email", "Invalid email format");
        
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Invalid email format", response.getBody().get("email"));
    }

    @Test
    @DisplayName("Test handle MethodArgumentNotValidException with no field errors")
    public void testHandleMethodArgumentNotValidExceptionWithNoErrors() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Test handle MethodArgumentNotValidException with multiple errors on same field")
    public void testHandleMethodArgumentNotValidExceptionWithMultipleErrorsSameField() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("employee", "name", "Name is required");
        FieldError fieldError2 = new FieldError("employee", "name", "Name must be at least 2 characters");
        
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        // Last error message should be used
        assertEquals("Name must be at least 2 characters", response.getBody().get("name"));
    }

    @Test
    @DisplayName("Test handle MethodArgumentNotValidException with null default message")
    public void testHandleMethodArgumentNotValidExceptionWithNullMessage() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("employee", "name", null, false, null, null, null);
        
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody().get("name"));
    }

    // ========== GENERIC EXCEPTION TESTS ==========

    @Test
    @DisplayName("Test handle generic Exception returns 500 Internal Server Error")
    public void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error occurred");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Test handle generic Exception with null message")
    public void testHandleGenericExceptionWithNullMessage() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle RuntimeException as generic exception")
    public void testHandleRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Runtime error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Runtime error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle NullPointerException as generic exception")
    public void testHandleNullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Null pointer error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle generic Exception with very long message")
    public void testHandleGenericExceptionWithLongMessage() {
        // Arrange
        String longMessage = "Error: " + "x".repeat(1000);
        Exception exception = new Exception(longMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(longMessage, response.getBody().getMessage());
    }

    // ========== ERROR RESPONSE STRUCTURE TESTS ==========

    @Test
    @DisplayName("Test ErrorResponse contains all required fields")
    public void testErrorResponseStructure() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getStatus() > 0);
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
    }

    @Test
    @DisplayName("Test ErrorResponse timestamp is recent")
    public void testErrorResponseTimestampIsRecent() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Test error");
        long beforeTime = System.currentTimeMillis();

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);
        long afterTime = System.currentTimeMillis();

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse.getTimestamp());
        
        long timestampMillis = errorResponse.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(timestampMillis >= beforeTime);
        assertTrue(timestampMillis <= afterTime + 1000); // Allow 1 second tolerance
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test handle exception with cause")
    public void testHandleExceptionWithCause() {
        // Arrange
        Exception cause = new Exception("Root cause");
        Exception exception = new Exception("Wrapper exception", cause);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Wrapper exception", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test handle exception with suppressed exceptions")
    public void testHandleExceptionWithSuppressed() {
        // Arrange
        Exception exception = new Exception("Main exception");
        exception.addSuppressed(new Exception("Suppressed 1"));
        exception.addSuppressed(new Exception("Suppressed 2"));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Main exception", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Test multiple exception handling calls")
    public void testMultipleExceptionHandlingCalls() {
        // Act
        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleIllegalArgument(
            new IllegalArgumentException("Error 1"));
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleAccessDenied(
            new AccessDeniedException("Error 2"));
        ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleGenericException(
            new Exception("Error 3"));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, response2.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response3.getStatusCode());
    }
}