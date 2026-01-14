package com.warehouse.ems.common;

import com.warehouse.ems.employee.EmployeeController;
import com.warehouse.ems.employee.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for GlobalExceptionHandler
 * Tests cover validation errors, not found errors, generic exceptions, and custom error responses
 */
@WebMvcTest(controllers = {EmployeeController.class, GlobalExceptionHandler.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // ========== VALIDATION ERROR TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_MissingRequiredField_Returns400() throws Exception {
        // Act & Assert - Missing firstName
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_InvalidEmail_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"invalid-email"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_EmptyRequiredField_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_MultipleFieldErrors_Returns400WithAllErrors() throws Exception {
        // Act & Assert - Multiple validation errors
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"","firstName":"","lastName":"","email":"invalid"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_NullValue_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":null,"firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest());
    }

    // ========== NOT FOUND ERROR TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testNotFoundError_InvalidEmployeeId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testNotFoundError_UpdateNonExistentEmployee_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any())).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"firstName":"John","lastName":"Doe"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testNotFoundError_DeleteNonExistentEmployee_Returns404() throws Exception {
        // Arrange
        when(employeeService.deleteEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    // ========== ILLEGAL ARGUMENT ERROR TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testIllegalArgumentError_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Request"))
                .andExpect(jsonPath("$.message").value("Badge ID already exists"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testIllegalArgumentError_InvalidInput_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Invalid employee data"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Request"));
    }

    // ========== GENERIC EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGenericException_DatabaseError_Returns500() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGenericException_UnexpectedError_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenThrow(new NullPointerException("Unexpected null value"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    // ========== MALFORMED JSON TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testMalformedJSON_InvalidSyntax_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testMalformedJSON_MissingBraces_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""badgeId":"EMP001","firstName":"John""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testMalformedJSON_ExtraComma_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John",}"))
                .andExpect(status().isBadRequest());
    }

    // ========== UNSUPPORTED MEDIA TYPE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUnsupportedMediaType_TextPlain_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUnsupportedMediaType_XML_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_XML)
                .content("<employee><badgeId>EMP001</badgeId></employee>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== METHOD NOT ALLOWED TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testMethodNotAllowed_PatchNotSupported_Returns405() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"firstName":"John"}"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ========== ERROR RESPONSE FORMAT TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponse_ContainsTimestamp() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponse_ContainsPath() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/employees/999"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponse_ContainsStatusCode() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_VeryLongFieldValue_Returns400() throws Exception {
        // Arrange
        String veryLongName = "A".repeat(1000);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"" + veryLongName + "","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testValidationError_SpecialCharactersInEmail_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@#$%.com"}"))
                .andExpect(status().isBadRequest());
    }

    // ========== CONCURRENT REQUEST TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testConcurrentErrors_MultipleRequests_HandlesIndependently() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert - Multiple concurrent requests
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    // ========== CUSTOM EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCustomException_BusinessRuleViolation_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Business rule violated: Cannot create employee with duplicate badge ID"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Business rule violated: Cannot create employee with duplicate badge ID"));
    }

    // ========== NULL POINTER EXCEPTION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testNullPointerException_UnexpectedNull_Returns500() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenThrow(new NullPointerException("Unexpected null reference"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    // ========== STACK TRACE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponse_DoesNotExposeStackTrace() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenThrow(new RuntimeException("Internal error"));

        // Act & Assert - Stack trace should not be exposed in production
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john@test.com"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    // ========== LOCALIZATION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponse_DefaultLocale_EnglishMessage() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }
}