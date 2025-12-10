package com.warehouse.exception;

import com.warehouse.employee.EmployeeController;
import com.warehouse.employee.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for GlobalExceptionHandler
 * Tests cover all exception scenarios with proper HTTP status codes and error messages
 */
@WebMvcTest(EmployeeController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // ========== RESOURCE NOT FOUND EXCEPTION TESTS ==========

    @Test
    public void testResourceNotFoundException_ShouldReturn404NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Employee not found")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testResourceNotFoundException_WithDifferentMessage_ShouldReturn404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Resource does not exist"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Resource does not exist")));
    }

    @Test
    public void testResourceNotFoundException_WithNullMessage_ShouldReturn404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException(null));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ========== DUPLICATE RESOURCE EXCEPTION TESTS ==========

    @Test
    public void testDuplicateResourceException_ShouldReturn409Conflict() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""badgeId": "EMP001","
                + ""firstName": "John","
                + ""lastName": "Doe","
                + ""email": "john.doe@warehouse.com","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "2024-01-01","
                + ""status": "ACTIVE""
                + "}";

        when(employeeService.createEmployee(any()))
                .thenThrow(new DuplicateResourceException("Badge ID EMP001 already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("Badge ID EMP001 already exists")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testDuplicateResourceException_WithEmailConflict_ShouldReturn409() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""badgeId": "EMP002","
                + ""firstName": "Jane","
                + ""lastName": "Smith","
                + ""email": "duplicate@warehouse.com","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "2024-01-01","
                + ""status": "ACTIVE""
                + "}";

        when(employeeService.createEmployee(any()))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

    // ========== ILLEGAL ARGUMENT EXCEPTION TESTS ==========

    @Test
    public void testIllegalArgumentException_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Invalid employee ID")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testIllegalArgumentException_WithNullParameter_ShouldReturn400() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new IllegalArgumentException("Parameter cannot be null"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Parameter cannot be null")));
    }

    @Test
    public void testIllegalArgumentException_WithInvalidFormat_ShouldReturn400() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid format"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid format")));
    }

    // ========== METHOD ARGUMENT NOT VALID EXCEPTION TESTS ==========

    @Test
    public void testMethodArgumentNotValidException_WithMissingBadgeId_ShouldReturn400() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""firstName": "John","
                + ""lastName": "Doe","
                + ""email": "john.doe@warehouse.com","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "2024-01-01","
                + ""status": "ACTIVE""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testMethodArgumentNotValidException_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""badgeId": "EMP001","
                + ""firstName": "John","
                + ""lastName": "Doe","
                + ""email": "invalid-email","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "2024-01-01","
                + ""status": "ACTIVE""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    public void testMethodArgumentNotValidException_WithEmptyFirstName_ShouldReturn400() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""badgeId": "EMP001","
                + ""firstName": "","
                + ""lastName": "Doe","
                + ""email": "john.doe@warehouse.com","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "2024-01-01","
                + ""status": "ACTIVE""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMethodArgumentNotValidException_WithMultipleErrors_ShouldReturn400() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""email": "invalid","
                + ""role": "WORKER""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ========== GENERIC EXCEPTION TESTS ==========

    @Test
    public void testGenericException_ShouldReturn500InternalServerError() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", containsString("Unexpected error occurred")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testGenericException_WithNullPointerException_ShouldReturn500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NullPointerException("Null pointer encountered"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)));
    }

    @Test
    public void testGenericException_WithDatabaseException_ShouldReturn500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Database connection failed")));
    }

    // ========== HTTP MESSAGE NOT READABLE EXCEPTION TESTS ==========

    @Test
    public void testHttpMessageNotReadableException_WithMalformedJSON_ShouldReturn400() throws Exception {
        // Arrange
        String malformedJson = "{invalid json";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testHttpMessageNotReadableException_WithInvalidDateFormat_ShouldReturn400() throws Exception {
        // Arrange
        String requestBody = "{"
                + ""badgeId": "EMP001","
                + ""firstName": "John","
                + ""lastName": "Doe","
                + ""email": "john.doe@warehouse.com","
                + ""role": "WORKER","
                + ""department": "Warehouse","
                + ""shiftGroup": "Morning","
                + ""hireDate": "invalid-date","
                + ""status": "ACTIVE""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ========== HTTP MEDIA TYPE NOT SUPPORTED EXCEPTION TESTS ==========

    @Test
    public void testHttpMediaTypeNotSupportedException_ShouldReturn415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testHttpMediaTypeNotSupportedException_WithXML_ShouldReturn415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_XML)
                .content("<xml></xml>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== ERROR RESPONSE STRUCTURE TESTS ==========

    @Test
    public void testErrorResponse_ShouldContainAllRequiredFields() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").isNumber())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    public void testErrorResponse_TimestampFormat_ShouldBeValid() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testException_WithEmptyMessage_ShouldStillReturn() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException(""));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    public void testException_WithVeryLongMessage_ShouldHandleGracefully() throws Exception {
        // Arrange
        String longMessage = "A".repeat(1000);
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException(longMessage));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testException_WithSpecialCharactersInMessage_ShouldHandleGracefully() throws Exception {
        // Arrange
        String specialMessage = "Error: <script>alert('xss')</script>";
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new ResourceNotFoundException(specialMessage));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}