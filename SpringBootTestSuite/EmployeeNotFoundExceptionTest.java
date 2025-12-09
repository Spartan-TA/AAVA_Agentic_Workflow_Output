package com.example.ems.employee;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeNotFoundExceptionTest {

    @Test
    public void testExceptionMessage() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Employee not found");
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    public void testExceptionWithNullMessage() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException(null);
        assertNull(ex.getMessage());
    }

    @Test
    public void testExceptionWithEmptyMessage() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("");
        assertEquals("", ex.getMessage());
    }

    @Test
    public void testExceptionIsRuntimeException() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Test");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    public void testExceptionWithLongMessage() {
        String longMessage = "Employee with ID 12345678901234567890 was not found in the database after extensive search";
        EmployeeNotFoundException ex = new EmployeeNotFoundException(longMessage);
        assertEquals(longMessage, ex.getMessage());
    }

    @Test
    public void testExceptionWithSpecialCharacters() {
        String specialMessage = "Employee not found: @#$%^&*()";
        EmployeeNotFoundException ex = new EmployeeNotFoundException(specialMessage);
        assertEquals(specialMessage, ex.getMessage());
    }
}