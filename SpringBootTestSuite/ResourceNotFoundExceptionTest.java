package com.wms.ems.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {
    // Test constructor with message
    @Test
    void testConstructor_Message_SetsMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        assertEquals("Not found", ex.getMessage());
    }

    // Test constructor with message and cause
    @Test
    void testConstructor_MessageAndCause_SetsMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found", cause);
        assertEquals("Not found", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // Test exception is instance of RuntimeException
    @Test
    void testIsInstanceOfRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");
        assertTrue(ex instanceof RuntimeException);
    }

    // Test thrown exception
    @Test
    void testThrowException_CatchesResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Not found");
        });
    }
}
