package SpringBootTestSuite;

import com.example.warehouse.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests handling of runtime, generic, and illegal argument exceptions.
 */
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleRuntimeException_Returns400() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("Bad input"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Bad input"));
    }

    @Test
    void testHandleException_Returns500() {
        ResponseEntity<?> response = handler.handleException(new Exception("Internal error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Internal error"));
    }

    @Test
    void testHandleIllegalArgumentException_Returns400() {
        ResponseEntity<?> response = handler.handleIllegalArgumentException(new IllegalArgumentException("Illegal arg"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Illegal arg"));
    }
}
