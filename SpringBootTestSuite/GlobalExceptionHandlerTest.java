package com.company.wems.exception;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Should handle validation exception and return BAD_REQUEST")
    void testHandleValidationException_ReturnsBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("employee", "name", "Name is required");
        FieldError fieldError2 = new FieldError("employee", "badgeId", "BadgeId is required");
        List<FieldError> errors = Arrays.asList(fieldError1, fieldError2);
        when(bindingResult.getFieldErrors()).thenReturn(errors);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.getError());
        assertTrue(body.getMessage().contains("Name is required"));
        assertTrue(body.getMessage().contains("BadgeId is required"));
        assertEquals("/api/test", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Should handle entity not found exception and return NOT_FOUND")
    void testHandleEntityNotFoundException_ReturnsNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Employee not found");
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.NOT_FOUND.value(), body.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), body.getError());
        assertEquals("Employee not found", body.getMessage());
        assertEquals("/api/test", body.getPath());
        assertNotNull(body.getTimestamp());
    }
}
