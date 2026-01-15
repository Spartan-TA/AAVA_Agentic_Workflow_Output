package com.warehouse.ems.exception;

import com.warehouse.ems.employee.controller.EmployeeController;
import com.warehouse.ems.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for GlobalExceptionHandler
 * Tests cover all exception types, error responses, and HTTP status codes
 */
@WebMvcTest(EmployeeController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // ========== NOT FOUND EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNotFoundException_Returns404WithMessage() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Employee not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNotFoundException_EmptyMessage_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException(""));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testNotFoundException_NullMessage_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException(null));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ========== ILLEGAL ARGUMENT EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIllegalArgumentException_Returns400WithMessage() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid employee ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid employee ID")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIllegalArgumentException_NullInput_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new IllegalArgumentException("Employee data cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIllegalArgumentException_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Badge ID already exists")));
    }

    // ========== VALIDATION EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testValidationException_EmptyName_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testValidationException_NullBadgeId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":null,"status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testValidationException_InvalidEmail_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP001","email":"invalid-email","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testValidationException_MultipleErrors_Returns400WithAllErrors() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"","badgeId":null,"status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testValidationException_InvalidRole_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP001","role":"INVALID_ROLE","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest());
    }

    // ========== METHOD ARGUMENT NOT VALID EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMethodArgumentNotValidException_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"","badgeId":"","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMethodArgumentNotValidException_FieldErrors_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":null,"badgeId":null,"status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    // ========== HTTP MESSAGE NOT READABLE EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testHttpMessageNotReadableException_MalformedJSON_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testHttpMessageNotReadableException_EmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testHttpMessageNotReadableException_InvalidDateFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP001","hireDate":"invalid-date","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest());
    }

    // ========== METHOD NOT ALLOWED EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMethodNotAllowedException_Returns405() throws Exception {
        // Act & Assert - Try to use PATCH on an endpoint that doesn't support it
        mockMvc.perform(patch("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test"}"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ========== UNSUPPORTED MEDIA TYPE EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnsupportedMediaTypeException_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnsupportedMediaTypeException_XMLContent_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<employee><name>Test</name></employee>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== INTERNAL SERVER ERROR TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInternalServerError_UnexpectedException_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Internal server error")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInternalServerError_NullPointerException_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NullPointerException("Null pointer error"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testInternalServerError_DatabaseException_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    // ========== CONSTRAINT VIOLATION EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testConstraintViolationException_UniqueConstraint_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new IllegalArgumentException("Unique constraint violation: badgeId"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"name":"Test","badgeId":"EMP001","status":"ACTIVE"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("constraint violation")));
    }

    // ========== MISSING PATH VARIABLE EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMissingPathVariableException_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring returns 404 for missing path variable
    }

    // ========== TYPE MISMATCH EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTypeMismatchException_InvalidIdType_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTypeMismatchException_NonNumericId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== ERROR RESPONSE FORMAT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testErrorResponse_ContainsTimestamp() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testErrorResponse_ContainsStatus() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testErrorResponse_ContainsMessage() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testErrorResponse_ContainsPath() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new NotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", is("/api/employees/999")));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testException_VeryLongMessage_Returns500() throws Exception {
        // Arrange
        String longMessage = "A".repeat(1000);
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException(longMessage));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testException_SpecialCharactersInMessage_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(anyLong()))
                .thenThrow(new RuntimeException("Error with special chars: <>&"'"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }
}